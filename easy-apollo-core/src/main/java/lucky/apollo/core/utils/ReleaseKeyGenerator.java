package lucky.apollo.core.utils;

import lucky.apollo.common.utils.UniqueKeyGenerator;
import lucky.apollo.core.entity.NamespacePO;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
public class ReleaseKeyGenerator extends UniqueKeyGenerator {
    public static String generateReleaseKey(NamespacePO namespace) {
        return generate(namespace.getAppId(), namespace.getNamespaceName());
    }
}
