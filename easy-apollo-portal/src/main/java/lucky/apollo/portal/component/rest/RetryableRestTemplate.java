package lucky.apollo.portal.component.rest;

import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.entity.dto.ServiceDTO;
import lucky.apollo.portal.adminsevice.AdminServiceAddressLocator;
import lucky.apollo.portal.metaservice.MetaDomainConsts;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import javax.annotation.PostConstruct;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/9/17
 */
@Slf4j
@Component
public class RetryableRestTemplate {
    @Autowired
    private RestTemplateFactory restTemplateFactory;

    private UriTemplateHandler uriTemplateHandler = new DefaultUriBuilderFactory();

    private RestTemplate restTemplate;

    @Autowired
    private AdminServiceAddressLocator adminServiceAddressLocator;

    @PostConstruct
    private void init() {
        restTemplate = restTemplateFactory.getObject();
    }

    public <T> T get(String path, Class<T> responseType, Object... urlVariables)
            throws RestClientException {
        return execute(HttpMethod.GET, path, null, responseType, urlVariables);
    }

    public <T> ResponseEntity<T> get(String path, ParameterizedTypeReference<T> reference,
                                     Object... uriVariables)
            throws RestClientException {

        return exchangeGet(path, reference, uriVariables);
    }

    public <T> T post(String path, Object request, Class<T> responseType, Object... uriVariables)
            throws RestClientException {
        return execute(HttpMethod.POST, path, request, responseType, uriVariables);
    }

    public void put(String path, Object request, Object... urlVariables) throws RestClientException {
        execute(HttpMethod.PUT, path, request, null, urlVariables);
    }

    public void delete(String path, Object... urlVariables) throws RestClientException {
        execute(HttpMethod.DELETE, path, null, null, urlVariables);
    }


    private <T> ResponseEntity<T> exchangeGet(String path, ParameterizedTypeReference<T> reference,
                                              Object... uriVariables) {
        if (path.startsWith("/")) {
            path = path.substring(1, path.length());
        }

        String uri = uriTemplateHandler.expand(path, uriVariables).getPath();

        List<ServiceDTO> services = getAdminServices();

        for (ServiceDTO serviceDTO : services) {
            try {

                ResponseEntity<T> result =
                        restTemplate.exchange(parseHost(serviceDTO) + path, HttpMethod.GET, null, reference, uriVariables);

                return result;
            } catch (Throwable t) {
                log.error("Http request failed, uri: {}, method: {}", uri, HttpMethod.GET, t);
                if (canRetry(t, HttpMethod.GET)) {
                    log.warn("retry uri : ", uri);
                } else {// biz exception rethrow
                    throw t;
                }

            }
        }

        //all admin server down
        ServiceException e =
                new ServiceException(String.format("Admin servers are unresponsive. meta server address: %s, admin servers: %s",
                        MetaDomainConsts.getDomain(), services));
        throw e;

    }


    private <T> T execute(HttpMethod method, String path, Object request, Class<T> responseType,
                          Object... uriVariables) {

        if (path.startsWith("/")) {
            path = path.substring(1, path.length());
        }

        String uri = uriTemplateHandler.expand(path, uriVariables).getPath();

        List<ServiceDTO> services = getAdminServices();

        for (ServiceDTO serviceDTO : services) {
            try {

                T result = doExecute(method, serviceDTO, path, request, responseType, uriVariables);

                return result;
            } catch (Throwable t) {
                log.error("Http request failed, uri: {}, method: {}", uri, method, t);
                if (canRetry(t, method)) {
                    log.warn("retry uri : ", uri);
                } else {//biz exception rethrow
                    throw t;
                }
            }
        }

        //all admin server down
        ServiceException e =
                new ServiceException(String.format("Admin servers are unresponsive. meta server address: %s, admin servers: %s",
                        MetaDomainConsts.getDomain(), services));
        throw e;
    }

    private <T> T doExecute(HttpMethod method, ServiceDTO service, String path, Object request,
                            Class<T> responseType,
                            Object... uriVariables) {
        T result = null;
        switch (method) {
            case GET:
                result = restTemplate.getForObject(parseHost(service) + path, responseType, uriVariables);
                break;
            case POST:
                result =
                        restTemplate.postForEntity(parseHost(service) + path, request, responseType, uriVariables).getBody();
                break;
            case PUT:
                restTemplate.put(parseHost(service) + path, request, uriVariables);
                break;
            case DELETE:
                restTemplate.delete(parseHost(service) + path, uriVariables);
                break;
            default:
                throw new UnsupportedOperationException(String.format("unsupported http method(method=%s)", method));
        }
        return result;
    }

    private String parseHost(ServiceDTO serviceAddress) {
        return serviceAddress.getHomepageUrl() + "/";
    }


    private List<ServiceDTO> getAdminServices() {

        List<ServiceDTO> services = adminServiceAddressLocator.getServiceList();

        if (CollectionUtils.isEmpty(services)) {
            ServiceException e = new ServiceException(String.format("No available admin server."
                            + " Maybe because of meta server down or all admin server down. "
                            + "Meta server address: %s",
                    MetaDomainConsts.getDomain()));
            throw e;
        }

        return services;
    }

    /**
     * post,delete,put请求在admin server处理超时情况下不重试
     *
     * @param e
     * @param method
     * @return
     */
    private boolean canRetry(Throwable e, HttpMethod method) {
        Throwable nestedException = e.getCause();
        if (method == HttpMethod.GET) {
            return nestedException instanceof SocketTimeoutException
                    || nestedException instanceof HttpHostConnectException
                    || nestedException instanceof ConnectTimeoutException;
        } else {
            return nestedException instanceof HttpHostConnectException
                    || nestedException instanceof ConnectTimeoutException;
        }
    }


}
