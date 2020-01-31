package lucky.apollo.core.eureka;

import lucky.apollo.core.config.ServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/9/18
 */
@Component
@Primary
public class ApolloEurekaClientConfig extends EurekaClientConfigBean {

    @Autowired
    private ServiceConfig serviceConfig;

    @Override
    public List<String> getEurekaServerServiceUrls(String myZone) {
        List<String> urls = serviceConfig.eurekaServiceUrls();
        return CollectionUtils.isEmpty(urls) ? super.getEurekaServerServiceUrls(myZone) : urls;
    }
}
