package lucky.apollo.client.util;

import com.google.common.base.Function;
import lucky.apollo.client.exception.ApolloConfigException;

import java.util.Date;

/**
 * @Author luckylau
 * @Date 2020/9/19
 */
public interface Functions {
    Function<String, Integer> TO_INT_FUNCTION = input -> Integer.parseInt(input);
    Function<String, Long> TO_LONG_FUNCTION = input -> Long.parseLong(input);
    Function<String, Short> TO_SHORT_FUNCTION = input -> Short.parseShort(input);
    Function<String, Float> TO_FLOAT_FUNCTION = input -> Float.parseFloat(input);
    Function<String, Double> TO_DOUBLE_FUNCTION = input -> Double.parseDouble(input);
    Function<String, Byte> TO_BYTE_FUNCTION = input -> Byte.parseByte(input);
    Function<String, Boolean> TO_BOOLEAN_FUNCTION = input -> Boolean.parseBoolean(input);
    Function<String, Date> TO_DATE_FUNCTION = input -> {
        try {
            return Parsers.forDate().parse(input);
        } catch (Exception ex) {
            throw new ApolloConfigException("Parse date failed", ex);
        }
    };
    Function<String, Long> TO_DURATION_FUNCTION = input -> {
        try {
            return Parsers.forDuration().parseToMillis(input);
        } catch (Exception ex) {
            throw new ApolloConfigException("Parse duration failed", ex);
        }
    };
}
