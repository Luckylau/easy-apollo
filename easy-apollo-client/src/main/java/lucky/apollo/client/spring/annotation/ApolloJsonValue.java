package lucky.apollo.client.spring.annotation;

import java.lang.annotation.*;

/**
 * @Author luckylau
 * @Date 2022/2/28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface ApolloJsonValue {
    /**
     * The actual value expression: e.g. "${someJsonPropertyKey:someDefaultValue}".
     */
    String value();
}
