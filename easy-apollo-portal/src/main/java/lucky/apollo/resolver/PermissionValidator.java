package lucky.apollo.resolver;

import lombok.extern.slf4j.Slf4j;
import lucky.apollo.constant.PermissionType;
import lucky.apollo.entity.po.AppNamespacePO;
import lucky.apollo.service.AppNamespaceService;
import lucky.apollo.service.RolePermissionService;
import lucky.apollo.service.UserService;
import lucky.apollo.utils.RoleUtils;
import org.springframework.stereotype.Component;

/**
 * @Author luckylau
 * @Date 2019/7/15
 */
@Slf4j
@Component("permissionValidator")
public class PermissionValidator {
    private final RolePermissionService rolePermissionService;

    private final AppNamespaceService appNamespaceService;

    private final UserService userService;

    public PermissionValidator(RolePermissionService rolePermissionService, AppNamespaceService appNamespaceService, UserService userService) {
        this.rolePermissionService = rolePermissionService;
        this.appNamespaceService = appNamespaceService;
        this.userService = userService;
    }

    public boolean isSuperAdmin() {
        return rolePermissionService.isSuperAdmin(userService.getCurrentUsername());
    }

    public boolean shouldHideConfigToCurrentUser(String appId, String namespaceName) {
        // 1. public namespace is open to every one
        AppNamespacePO appNamespace = appNamespaceService.findByAppIdAndName(appId, namespaceName);
        if (appNamespace != null && appNamespace.isPublic()) {
            return false;
        }

        // 2. check app admin and operate permissions
        return !isAppAdmin(appId) && !hasOperateNamespacePermission(appId, namespaceName);
    }

    public boolean hasAssignRolePermission(String appId) {
        return rolePermissionService.userHasPermission(userService.getCurrentUsername(),
                PermissionType.ASSIGN_ROLE,
                appId);
    }

    public boolean isAppAdmin(String appId) {
        return isSuperAdmin() || hasAssignRolePermission(appId);
    }

    public boolean hasOperateNamespacePermission(String appId, String namespaceName) {
        return hasModifyNamespacePermission(appId, namespaceName) || hasReleaseNamespacePermission(appId, namespaceName);
    }

    public boolean hasModifyNamespacePermission(String appId, String namespaceName) {
        return rolePermissionService.userHasPermission(userService.getCurrentUsername(),
                PermissionType.MODIFY_NAMESPACE,
                RoleUtils.buildNamespaceTargetId(appId, namespaceName));
    }

    public boolean hasReleaseNamespacePermission(String appId, String namespaceName) {
        return rolePermissionService.userHasPermission(userService.getCurrentUsername(),
                PermissionType.RELEASE_NAMESPACE,
                RoleUtils.buildNamespaceTargetId(appId, namespaceName));
    }


}