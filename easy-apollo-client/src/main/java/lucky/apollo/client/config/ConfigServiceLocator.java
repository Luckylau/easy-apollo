package lucky.apollo.client.config;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.build.ApolloInjector;
import lucky.apollo.client.exception.ApolloConfigException;
import lucky.apollo.client.foundation.Foundation;
import lucky.apollo.client.util.ConfigUtil;
import lucky.apollo.client.util.ExceptionUtil;
import lucky.apollo.client.util.http.HttpRequest;
import lucky.apollo.client.util.http.HttpResponse;
import lucky.apollo.client.util.http.HttpUtil;
import lucky.apollo.common.constant.ServiceNameConsts;
import lucky.apollo.common.entity.dto.ServiceDTO;
import lucky.apollo.common.utils.ApolloThreadFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author luckylau
 * @Date 2020/10/12
 */
@Slf4j
public class ConfigServiceLocator {
    private HttpUtil m_httpUtil;
    private ConfigUtil m_configUtil;
    private AtomicReference<List<ServiceDTO>> m_configServices;
    private Type m_responseType;
    private ScheduledExecutorService m_executorService;
    private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");
    private static final Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();

    /**
     * Create a config service locator.
     */
    public ConfigServiceLocator() {
        List<ServiceDTO> initial = Lists.newArrayList();
        this.m_configServices = new AtomicReference<>(initial);
        this.m_responseType = new TypeToken<List<ServiceDTO>>() {
        }.getType();
        this.m_httpUtil = ApolloInjector.getInstance(HttpUtil.class);
        this.m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
        this.m_executorService = new ScheduledThreadPoolExecutor(1,
                ApolloThreadFactory.create("ConfigServiceLocator", true));
        initConfigServices();
    }

    private void initConfigServices() {
        // get from run time configurations
        List<ServiceDTO> customizedConfigServices = getCustomizedConfigService();

        if (customizedConfigServices != null) {
            setConfigServices(customizedConfigServices);
            return;
        }

        // update from meta service
        this.tryUpdateConfigServices();
        this.schedulePeriodicRefresh();
    }

    private void schedulePeriodicRefresh() {
        this.m_executorService.scheduleAtFixedRate(
                () -> {
                    log.debug("refresh config services");
                    tryUpdateConfigServices();
                }, m_configUtil.getRefreshInterval(), m_configUtil.getRefreshInterval(),
                m_configUtil.getRefreshIntervalTimeUnit());
    }

    private void setConfigServices(List<ServiceDTO> services) {
        m_configServices.set(services);
        logConfigServices(services);
    }

    private void logConfigServices(List<ServiceDTO> serviceDtos) {
        for (ServiceDTO serviceDto : serviceDtos) {
            logConfigService(serviceDto.getHomepageUrl());
        }
    }

    private void logConfigService(String serviceUrl) {
        log.info("Apollo.Config.Services", serviceUrl);
    }

    private boolean tryUpdateConfigServices() {
        try {
            updateConfigServices();
            return true;
        } catch (Throwable ex) {
            //ignore
        }
        return false;
    }

    private synchronized void updateConfigServices() {
        String url = assembleMetaServiceUrl();

        HttpRequest request = new HttpRequest(url);
        int maxRetries = 2;
        Throwable exception = null;

        for (int i = 0; i < maxRetries; i++) {
            try {
                HttpResponse<List<ServiceDTO>> response = m_httpUtil.doGet(request, m_responseType);
                List<ServiceDTO> services = response.getBody();
                if (services == null || services.isEmpty()) {
                    logConfigService("Empty response!");
                    continue;
                }
                setConfigServices(services);
                return;
            } catch (Throwable ex) {
                log.error("ApolloConfigException", ExceptionUtil.getDetailMessage(ex));
            }

            try {
                m_configUtil.getOnErrorRetryIntervalTimeUnit().sleep(m_configUtil.getOnErrorRetryInterval());
            } catch (InterruptedException ex) {
                //ignore
            }
        }

        throw new ApolloConfigException(
                String.format("Get config services failed from %s", url), exception);
    }

    private String assembleMetaServiceUrl() {
        String domainName = m_configUtil.getMetaServerDomainName();
        String appId = m_configUtil.getAppId();
        String localIp = m_configUtil.getLocalIp();

        Map<String, String> queryParams = Maps.newHashMap();
        queryParams.put("appId", queryParamEscaper.escape(appId));
        if (!Strings.isNullOrEmpty(localIp)) {
            queryParams.put("ip", queryParamEscaper.escape(localIp));
        }

        return domainName + "/services/config?" + MAP_JOINER.join(queryParams);
    }

    private List<ServiceDTO> getCustomizedConfigService() {
        // 1. Get from System Property
        String configServices = System.getProperty("apollo.configService");
        if (Strings.isNullOrEmpty(configServices)) {
            // 2. Get from OS environment variable
            configServices = System.getenv("APOLLO_CONFIGSERVICE");
        }
        if (Strings.isNullOrEmpty(configServices)) {
            // 3. Get from server.properties
            configServices = Foundation.server().getProperty("apollo.configService", null);
        }

        if (Strings.isNullOrEmpty(configServices)) {
            return null;
        }

        log.warn("Located config services from apollo.configService configuration: {}, will not refresh config services from remote meta service!", configServices);

        // mock service dto list
        String[] configServiceUrls = configServices.split(",");
        List<ServiceDTO> serviceDTOS = Lists.newArrayList();

        for (String configServiceUrl : configServiceUrls) {
            configServiceUrl = configServiceUrl.trim();
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setHomepageUrl(configServiceUrl);
            serviceDTO.setAppName(ServiceNameConsts.APOLLO_CONFIGSERVICE);
            serviceDTO.setInstanceId(configServiceUrl);
            serviceDTOS.add(serviceDTO);
        }

        return serviceDTOS;
    }

    /**
     * Get the config service info from remote meta server.
     *
     * @return the services dto
     */
    public List<ServiceDTO> getConfigServices() {
        if (m_configServices.get().isEmpty()) {
            updateConfigServices();
        }

        return m_configServices.get();
    }


}
