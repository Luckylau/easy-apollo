package lucky.apollo.client.spring.annotation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import lucky.apollo.client.config.Config;
import lucky.apollo.client.config.ConfigChangeListener;
import lucky.apollo.client.model.ConfigChangeEvent;
import lucky.apollo.client.service.ConfigService;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2022/2/27
 */
public class ApolloAnnotationProcessor extends ApolloProcessor {

    @Override
    protected void processField(Object bean, String beanName, Field field) {
        ApolloConfig annotation = AnnotationUtils.getAnnotation(field, ApolloConfig.class);
        if (annotation == null) {
            return;
        }

        Preconditions.checkArgument(Config.class.isAssignableFrom(field.getType()),
                "Invalid type: %s for field: %s, should be Config", field.getType(), field);

        String namespace = annotation.value();
        Config config = ConfigService.getConfig(namespace);

        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, bean, config);
    }

    @Override
    protected void processMethod(final Object bean, String beanName, final Method method) {
        ApolloConfigChangeListener annotation = AnnotationUtils
                .findAnnotation(method, ApolloConfigChangeListener.class);
        if (annotation == null) {
            return;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        Preconditions.checkArgument(parameterTypes.length == 1,
                "Invalid number of parameters: %s for method: %s, should be 1", parameterTypes.length,
                method);
        Preconditions.checkArgument(ConfigChangeEvent.class.isAssignableFrom(parameterTypes[0]),
                "Invalid parameter type: %s for method: %s, should be ConfigChangeEvent", parameterTypes[0],
                method);

        ReflectionUtils.makeAccessible(method);
        String[] namespaces = annotation.value();
        String[] annotatedInterestedKeys = annotation.interestedKeys();
        Set<String> interestedKeys = annotatedInterestedKeys.length > 0 ? Sets.newHashSet(annotatedInterestedKeys) : null;
        ConfigChangeListener configChangeListener = changeEvent -> ReflectionUtils.invokeMethod(method, bean, changeEvent);

        for (String namespace : namespaces) {
            Config config = ConfigService.getConfig(namespace);

            if (interestedKeys == null) {
                config.addChangeListener(configChangeListener);
            } else {
                config.addChangeListener(configChangeListener, interestedKeys);
            }
        }
    }
}
