package lucky.apollo.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lucky.apollo.constant.RoleType;

import java.util.Iterator;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
public class RoleUtils {
    private static final Splitter STRING_SPLITTER = Splitter.on("+").omitEmptyStrings().trimResults();
    private static final Joiner STRING_JOINER = Joiner.on("+").skipNulls();


    public static String extractAppIdFromRoleName(String roleName) {
        Iterator<String> parts = STRING_SPLITTER.split(roleName).iterator();
        if (parts.hasNext()) {
            String roleType = parts.next();
            if (RoleType.isValidRoleType(roleType) && parts.hasNext()) {
                return parts.next();
            }
        }
        return null;
    }

    public static String buildAppMasterRoleName(String appId) {
        return STRING_JOINER.join(RoleType.MASTER, appId);
    }

    public static String buildNamespaceTargetId(String appId, String namespaceName) {
        return buildNamespaceTargetId(appId, namespaceName, null);
    }

    public static String buildNamespaceTargetId(String appId, String namespaceName, String env) {
        return STRING_JOINER.join(appId, namespaceName, env);
    }

}