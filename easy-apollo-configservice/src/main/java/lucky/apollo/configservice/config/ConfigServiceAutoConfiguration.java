package lucky.apollo.configservice.config;

import lucky.apollo.configservice.cache.ConfigServiceWithCache;
import lucky.apollo.configservice.service.ConfigService;
import lucky.apollo.configservice.service.impl.DefaultConfigServiceImpl;
import lucky.apollo.core.config.ServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author luckylau
 * @Date 2019/12/6
 */
@Configuration
public class ConfigServiceAutoConfiguration {

    private final ServiceConfig serviceConfig;

    public ConfigServiceAutoConfiguration(final ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    @Bean
    public ConfigService configService() {
        if (serviceConfig.isConfigServiceCacheEnabled()) {
            return new ConfigServiceWithCache();
        }
        return new DefaultConfigServiceImpl();
    }


}
