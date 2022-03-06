package lucky.apollo.client.spring.boot;

import lucky.apollo.client.spring.config.ConfigPropertySourcesProcessor;
import lucky.apollo.client.spring.config.PropertySourcesConstants;
import lucky.apollo.client.spring.config.PropertySourcesProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author luckylau
 * @Date 2022/3/6
 */
@Configuration
@ConditionalOnProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED)
@ConditionalOnMissingBean(PropertySourcesProcessor.class)
public class ApolloAutoConfiguration {
    @Bean
    public ConfigPropertySourcesProcessor configPropertySourcesProcessor() {
        return new ConfigPropertySourcesProcessor();
    }
}
