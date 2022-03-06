package lucky.apollo.client.spring.property;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lucky.apollo.client.build.ApolloInjector;
import lucky.apollo.client.spring.util.SpringInjector;
import lucky.apollo.client.util.ConfigUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2022/2/28
 */
public class SpringValueDefinitionProcessor implements BeanDefinitionRegistryPostProcessor {
    private static final Map<BeanDefinitionRegistry, Multimap<String, SpringValueDefinition>> beanName2SpringValueDefinitions =
            Maps.newConcurrentMap();
    private static final Set<BeanDefinitionRegistry> PROPERTY_VALUES_PROCESSED_BEAN_FACTORIES = Sets.newConcurrentHashSet();

    private final ConfigUtil configUtil;
    private final PlaceholderHelper placeholderHelper;

    public SpringValueDefinitionProcessor() {
        configUtil = ApolloInjector.getInstance(ConfigUtil.class);
        placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
    }

    public static Multimap<String, SpringValueDefinition> getBeanName2SpringValueDefinitions(BeanDefinitionRegistry registry) {
        Multimap<String, SpringValueDefinition> springValueDefinitions = beanName2SpringValueDefinitions.get(registry);
        if (springValueDefinitions == null) {
            springValueDefinitions = LinkedListMultimap.create();
        }

        return springValueDefinitions;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
            processPropertyValues(beanDefinitionRegistry);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    private void processPropertyValues(BeanDefinitionRegistry beanRegistry) {
        if (!PROPERTY_VALUES_PROCESSED_BEAN_FACTORIES.add(beanRegistry)) {
            // already initialized
            return;
        }

        if (!beanName2SpringValueDefinitions.containsKey(beanRegistry)) {
            beanName2SpringValueDefinitions.put(beanRegistry, LinkedListMultimap.<String, SpringValueDefinition>create());
        }

        Multimap<String, SpringValueDefinition> springValueDefinitions = beanName2SpringValueDefinitions.get(beanRegistry);

        String[] beanNames = beanRegistry.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanRegistry.getBeanDefinition(beanName);
            MutablePropertyValues mutablePropertyValues = beanDefinition.getPropertyValues();
            List<PropertyValue> propertyValues = mutablePropertyValues.getPropertyValueList();
            for (PropertyValue propertyValue : propertyValues) {
                Object value = propertyValue.getValue();
                if (!(value instanceof TypedStringValue)) {
                    continue;
                }
                String placeholder = ((TypedStringValue) value).getValue();
                Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeholder);

                if (keys.isEmpty()) {
                    continue;
                }

                for (String key : keys) {
                    springValueDefinitions.put(beanName, new SpringValueDefinition(key, placeholder, propertyValue.getName()));
                }
            }
        }
    }
}
