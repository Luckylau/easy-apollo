package lucky.apollo.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
public class Validator {
    public static final String INVALID_APP_CLUSTER_NAMESPACE_MESSAGE = "只允许输入数字，字母和符号 - _ .";
    public static final String INVALID_NAMESPACE_MESSAGE = "不允许以.json, .yml, .yaml, .xml, .properties结尾";
    public static final String APP_CLUSTER_NAMESPACE_VALIDATOR = "[0-9a-zA-Z_.-]+";
    private static final String NAMESPACE_VALIDATOR = "[a-zA-Z0-9._-]+(?<!\\.(properties))$";
    private static final Pattern CLUSTER_NAMESPACE_PATTERN =
            Pattern.compile(APP_CLUSTER_NAMESPACE_VALIDATOR);
    private static final Pattern APP_NAMESPACE_PATTERN =
            Pattern.compile(NAMESPACE_VALIDATOR);

    public static boolean isValidAppNamespace(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return CLUSTER_NAMESPACE_PATTERN.matcher(name).matches() && APP_NAMESPACE_PATTERN.matcher(name).matches();
    }
}