package lucky.apollo.core.utils;

import com.google.common.base.Joiner;
import lucky.apollo.common.constant.ConfigConsts;


/**
 * @Author luckylau
 * @Date 2019/7/25
 */
public class ReleaseMessageKeyGenerator {
    private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.APP_NAMESPACE_SEPARATOR);

    public static String generate(String appId, String namespace) {
        return STRING_JOINER.join(appId, namespace);
    }
}
