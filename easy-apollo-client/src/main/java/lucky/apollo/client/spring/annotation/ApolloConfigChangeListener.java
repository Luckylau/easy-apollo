package lucky.apollo.client.spring.annotation;

import lucky.apollo.common.constant.ConfigConsts;

import java.lang.annotation.*;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ApolloConfigChangeListener {
    /**
     * Apollo namespace for the config, if not specified then default to application
     */
    String[] value() default {ConfigConsts.NAMESPACE_APPLICATION};

    /**
     * The keys interested by the listener, will only be notified if any of the interested keys is changed.
     * <br />
     * If neither of {@code interestedKeys} and {@code interestedKeyPrefixes} is specified then the {@code listener} will be notified when any key is changed.
     */
    String[] interestedKeys() default {};
}
