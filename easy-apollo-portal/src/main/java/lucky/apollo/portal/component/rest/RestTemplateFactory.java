package lucky.apollo.portal.component.rest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @Author luckylau
 * @Date 2019/9/17
 */
@Component
public class RestTemplateFactory implements FactoryBean<RestTemplate>, InitializingBean {

    private final int readTimeout = 10000;

    private final int connectTimeout = 3000;

    @Resource(name = "messageConverters")
    private HttpMessageConverters messageConverters;


    private RestTemplate restTemplate;

    @Override
    public RestTemplate getObject() {
        return restTemplate;
    }

    @Override
    public Class<?> getObjectType() {
        return RestTemplate.class;
    }

    @Override
    public void afterPropertiesSet() {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        restTemplate = new RestTemplate(messageConverters.getConverters());

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        requestFactory.setConnectTimeout(connectTimeout);

        requestFactory.setReadTimeout(readTimeout);

        restTemplate.setRequestFactory(requestFactory);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
