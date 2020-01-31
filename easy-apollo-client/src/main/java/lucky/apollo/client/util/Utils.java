package lucky.apollo.client.util;

import com.google.common.base.Strings;

/**
 * @Author luckylau
 * @Date 2019/12/17
 */
public class Utils {
    public static boolean isBlank(String str) {
        return Strings.nullToEmpty(str).trim().isEmpty();
    }

    public static boolean isOSWindows() {
        String osName = System.getProperty("os.name");
        if (Utils.isBlank(osName)) {
            return false;
        }
        return osName.startsWith("Windows");
    }
}
