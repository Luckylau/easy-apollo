package lucky.apollo.client.spring.config;

import lucky.apollo.client.spring.annotation.ApolloAnnotationProcessor;
import lucky.apollo.client.spring.annotation.ApolloJsonValueProcessor;
import lucky.apollo.client.spring.annotation.SpringValueProcessor;
import lucky.apollo.client.spring.property.SpringValueDefinitionProcessor;
import lucky.apollo.client.spring.util.BeanRegistrationUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author luckylau
 * @Date 2022/2/28
 */
public class ConfigPropertySourcesProcessor extends PropertySourcesProcessor
        implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Map<String, Object> propertySourcesPlaceholderPropertyValues = new HashMap<>();
        // to make sure the default PropertySourcesPlaceholderConfigurer's priority is higher than PropertyPlaceholderConfigurer
        propertySourcesPlaceholderPropertyValues.put("order", 0);

        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, PropertySourcesPlaceholderConfigurer.class.getName(),
                PropertySourcesPlaceholderConfigurer.class, propertySourcesPlaceholderPropertyValues);
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloAnnotationProcessor.class.getName(),
                ApolloAnnotationProcessor.class);
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueProcessor.class.getName(), SpringValueProcessor.class);
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloJsonValueProcessor.class.getName(),
                ApolloJsonValueProcessor.class);

        processSpringValueDefinition(registry);
    }

    /**
     * For Spring 3.x versions, the BeanDefinitionRegistryPostProcessor would not be
     * instantiated if it is added in postProcessBeanDefinitionRegistry phase, so we have to manually
     * call the postProcessBeanDefinitionRegistry method of SpringValueDefinitionProcessor here...
     */
    private void processSpringValueDefinition(BeanDefinitionRegistry registry) {
        SpringValueDefinitionProcessor springValueDefinitionProcessor = new SpringValueDefinitionProcessor();

        springValueDefinitionProcessor.postProcessBeanDefinitionRegistry(registry);
    }
}
