package lucky.apollo.common.utils;

import lucky.apollo.common.constant.Env;

/**
 * @Author luckylau
 * @Date 2020/3/11
 */
public class EnvUtils {
    public static Env transformEnv(String envName) {
        if (StringUtils.isBlank(envName)) {
            return Env.UNKNOWN;
        }
        switch (envName.trim().toUpperCase()) {
            case "PRO":
            case "PROD": //just in case
                return Env.PRO;
            case "DEV":
                return Env.DEV;
            default:
                return Env.UNKNOWN;
        }
    }
}
