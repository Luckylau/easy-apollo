package lucky.apollo.common.constant;

import org.springframework.util.StringUtils;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
public enum ConfigFileFormat {
    /**
     * properties格式
     */
    Properties("properties"),
    /**
     * json格式
     */
    XML("xml"), JSON("json"),
    /**
     * yaml格式
     */
    YML("yml"), YAML("yaml");

    private String value;

    ConfigFileFormat(String value) {
        this.value = value;
    }

    public static ConfigFileFormat fromString(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("value can not be empty");
        }
        switch (value.toLowerCase()) {
            case "properties":
                return Properties;
            case "xml":
                return XML;
            case "json":
                return JSON;
            case "yml":
                return YML;
            case "yaml":
                return YAML;
            default:
                throw new IllegalArgumentException(value + " can not map enum");
        }
    }

    public static boolean isValidFormat(String value) {
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isPropertiesCompatible(ConfigFileFormat format) {
        return format == YAML || format == YML;
    }

    public String getValue() {
        return value;
    }
}