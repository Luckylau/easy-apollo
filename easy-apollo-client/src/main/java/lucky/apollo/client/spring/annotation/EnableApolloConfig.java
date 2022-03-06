package lucky.apollo.client.spring.annotation;

import lucky.apollo.common.constant.ConfigConsts;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

/**
 * @Author luckylau
 * @Date 2022/2/28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ApolloConfigRegistrar.class)
public @interface EnableApolloConfig {

    /**
     * Apollo namespaces to inject configuration into Spring Property Sources.
     */
    String[] value() default {
            ConfigConsts.NAMESPACE_APPLICATION};

    /**
     * The order of the apollo config, default is {@link Ordered#LOWEST_PRECEDENCE}, which is Integer.MAX_VALUE.
     * If there are properties with the same name in different apollo configs, the apollo config with smaller order wins.
     *
     * @return
     */
    int order() default Ordered.LOWEST_PRECEDENCE;
}
