package lucky.apollo.client.spring.annotation;

import lucky.apollo.common.constant.ConfigConsts;

import java.lang.annotation.*;

/**
 * @Author liuJun
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

    /**
     * The key prefixes that the listener is interested in, will be notified if and only if the changed keys start with anyone of the prefixes.
     * The prefixes will simply be used to determine whether the {@code listener} should be notified or not using {@code changedKey.startsWith(prefix)}.
     * e.g. "spring." means that {@code listener} is interested in keys that starts with "spring.", such as "spring.banner", "spring.jpa", etc.
     * and "application" means that {@code listener} is interested in keys that starts with "application", such as "applicationName", "application.port", etc.
     * <br />
     * If neither of {@code interestedKeys} and {@code interestedKeyPrefixes} is specified then the {@code listener} will be notified when whatever key is changed.
     */
    String[] interestedKeyPrefixes() default {};
}
