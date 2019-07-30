package lucky.apollo.common.utils;

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

    /**
     * <p>
     * Checks if a String is empty ("") or null.
     * </p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>
     * NOTE: This method changed in Lang version 2.0. It no longer trims the String. That functionality is available in isBlank().
     * </p>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}