package lucky.apollo.client.spring.annotation;

import com.google.common.collect.Lists;
import lucky.apollo.client.spring.config.PropertySourcesProcessor;
import lucky.apollo.client.spring.property.SpringValueDefinitionProcessor;
import lucky.apollo.client.spring.util.BeanRegistrationUtil;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author luckylau
 * @Date 2022/2/28
 */
public class ApolloConfigRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(annotationMetadata
                .getAnnotationAttributes(EnableApolloConfig.class.getName()));
        String[] namespaces = attributes.getStringArray("value");
        int order = attributes.getNumber("order");
        PropertySourcesProcessor.addNamespaces(Lists.newArrayList(namespaces), order);

        Map<String, Object> propertySourcesPlaceholderPropertyValues = new HashMap<>();
        // to make sure the default PropertySourcesPlaceholderConfigurer's priority is higher than PropertyPlaceholderConfigurer
        propertySourcesPlaceholderPropertyValues.put("order", 0);

        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, PropertySourcesPlaceholderConfigurer.class.getName(),
                PropertySourcesPlaceholderConfigurer.class, propertySourcesPlaceholderPropertyValues);

        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, PropertySourcesProcessor.class.getName(),
                PropertySourcesProcessor.class);

        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloAnnotationProcessor.class.getName(),
                ApolloAnnotationProcessor.class);

        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueProcessor.class.getName(), SpringValueProcessor.class);
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueDefinitionProcessor.class.getName(), SpringValueDefinitionProcessor.class);

        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloJsonValueProcessor.class.getName(),
                ApolloJsonValueProcessor.class);
    }
}
