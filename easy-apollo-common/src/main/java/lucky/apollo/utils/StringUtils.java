package lucky.apollo.utils;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
public class StringUtils {

    public static boolean isContainEmpty(String... args) {
        if (args == null) {
            return false;
        }

        for (String arg : args) {
            if (arg == null || "".equals(arg)) {
                return true;
            }
        }

        return false;
    }
}