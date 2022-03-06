package lucky.apollo.client.spring.annotation;

import lucky.apollo.common.constant.ConfigConsts;

import java.lang.annotation.*;

/**
 * @Author luckylau
 * @Date 2022/2/27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface ApolloConfig {
    /**
     * Apollo namespace for the config, if not specified then default to application
     */
    String value() default ConfigConsts.NAMESPACE_APPLICATION;
}
