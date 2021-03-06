package lucky.apollo.portal.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lucky.apollo.portal.constant.RoleType;

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
        return STRING_JOINER.join(appId, namespaceName);
    }

    public static String buildNamespaceRoleName(String appId, String namespaceName, String roleType) {
        return STRING_JOINER.join(roleType, appId, namespaceName);
    }

    public static String buildManageAppMasterRoleName(String permissionType, String permissionTargetId) {
        return STRING_JOINER.join(permissionType, permissionTargetId);
    }

    public static String buildModifyNamespaceRoleName(String appId, String namespaceName) {
        return STRING_JOINER.join(RoleType.MODIFY_NAMESPACE, appId, namespaceName);
    }

    public static String buildReleaseNamespaceRoleName(String appId, String namespaceName) {
        return STRING_JOINER.join(RoleType.RELEASE_NAMESPACE, appId, namespaceName);
    }

    public static String buildCreateApplicationRoleName(String permissionType, String permissionTargetId) {
        return STRING_JOINER.join(permissionType, permissionTargetId);
    }

    public static String buildAppRoleName(String appId, String roleType) {
        return STRING_JOINER.join(roleType, appId);
    }


}