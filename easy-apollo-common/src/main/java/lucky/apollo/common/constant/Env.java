package lucky.apollo.common.constant;

import org.apache.commons.lang.StringUtils;

/**
 * @Author luckylau
 * @Date 2019/8/12
 */
public enum Env {
    DEV, TEST, PRO, UNKNOWN;

    public static Env fromString(String env) {
        return transformEnv(env);
    }

    private static Env transformEnv(String envName) {
        if (StringUtils.isBlank(envName)) {
            return Env.UNKNOWN;
        }
        switch (envName.trim().toUpperCase()) {
            case "TEST":
                return Env.TEST;
            case "PRO":
                return Env.PRO;
            case "DEV":
                return Env.DEV;
            default:
                return Env.UNKNOWN;
        }
    }
}
