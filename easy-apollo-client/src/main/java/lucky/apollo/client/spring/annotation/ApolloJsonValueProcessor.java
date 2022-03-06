package lucky.apollo.client.spring.annotation;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.build.ApolloInjector;
import lucky.apollo.client.spring.property.PlaceholderHelper;
import lucky.apollo.client.spring.property.SpringValue;
import lucky.apollo.client.spring.property.SpringValueRegistry;
import lucky.apollo.client.spring.util.SpringInjector;
import lucky.apollo.client.util.ConfigUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2022/2/28
 */
@Slf4j
public class ApolloJsonValueProcessor extends ApolloProcessor implements BeanFactoryAware {
    private static final Gson gson = new Gson();

    private final ConfigUtil configUtil;
    private final PlaceholderHelper placeholderHelper;
    private final SpringValueRegistry springValueRegistry;
    private ConfigurableBeanFactory beanFactory;

    public ApolloJsonValueProcessor() {
        configUtil = ApolloInjector.getInstance(ConfigUtil.class);
        placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
        springValueRegistry = SpringInjector.getInstance(SpringValueRegistry.class);
    }

    @Override
    protected void processField(Object bean, String beanName, Field field) {
        ApolloJsonValue apolloJsonValue = AnnotationUtils.getAnnotation(field, ApolloJsonValue.class);
        if (apolloJsonValue == null) {
            return;
        }
        String placeholder = apolloJsonValue.value();
        Object propertyValue = placeholderHelper
                .resolvePropertyValue(beanFactory, beanName, placeholder);

        // propertyValue will never be null, as @ApolloJsonValue will not allow that
        if (!(propertyValue instanceof String)) {
            return;
        }

        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        ReflectionUtils
                .setField(field, bean, parseJsonValue((String) propertyValue, field.getGenericType()));
        field.setAccessible(accessible);

        if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
            Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeholder);
            for (String key : keys) {
                SpringValue springValue = new SpringValue(key, placeholder, bean, beanName, field, true);
                springValueRegistry.register(beanFactory, key, springValue);
                log.debug("Monitoring {}", springValue);
            }
        }
    }

    @Override
    protected void processMethod(Object bean, String beanName, Method method) {
        ApolloJsonValue apolloJsonValue = AnnotationUtils.getAnnotation(method, ApolloJsonValue.class);
        if (apolloJsonValue == null) {
            return;
        }
        String placeHolder = apolloJsonValue.value();

        Object propertyValue = placeholderHelper
                .resolvePropertyValue(beanFactory, beanName, placeHolder);

        // propertyValue will never be null, as @ApolloJsonValue will not allow that
        if (!(propertyValue instanceof String)) {
            return;
        }

        Type[] types = method.getGenericParameterTypes();
        Preconditions.checkArgument(types.length == 1,
                "Ignore @Value setter {}.{}, expecting 1 parameter, actual {} parameters",
                bean.getClass().getName(), method.getName(), method.getParameterTypes().length);

        boolean accessible = method.isAccessible();
        method.setAccessible(true);
        ReflectionUtils.invokeMethod(method, bean, parseJsonValue((String) propertyValue, types[0]));
        method.setAccessible(accessible);

        if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
            Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeHolder);
            for (String key : keys) {
                SpringValue springValue = new SpringValue(key, apolloJsonValue.value(), bean, beanName,
                        method, true);
                springValueRegistry.register(beanFactory, key, springValue);
                log.debug("Monitoring {}", springValue);
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableBeanFactory) beanFactory;
    }


    private Object parseJsonValue(String json, Type targetType) {
        try {
            return gson.fromJson(json, targetType);
        } catch (Throwable ex) {
            log.error("Parsing json '{}' to type {} failed!", json, targetType, ex);
            throw ex;
        }
    }
}
