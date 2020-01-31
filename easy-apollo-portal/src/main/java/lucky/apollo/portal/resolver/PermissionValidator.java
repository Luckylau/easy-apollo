package lucky.apollo.portal.resolver;

import lombok.extern.slf4j.Slf4j;
import lucky.apollo.portal.constant.PermissionType;
import lucky.apollo.portal.service.AppNamespaceService;
import lucky.apollo.portal.service.RolePermissionService;
import lucky.apollo.portal.service.UserService;
import lucky.apollo.portal.utils.RoleUtils;
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
        return rolePermissionService.isSuperAdmin(userService.getCurrentUser().getUserId());
    }

    public boolean hasAssignRolePermission(String appId) {
        return rolePermissionService.userHasPermission(userService.getCurrentUser().getUserId(),
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
        return rolePermissionService.userHasPermission(userService.getCurrentUser().getUserId(),
                PermissionType.MODIFY_NAMESPACE,
                RoleUtils.buildNamespaceTargetId(appId, namespaceName));
    }

    public boolean hasReleaseNamespacePermission(String appId, String namespaceName) {
        return rolePermissionService.userHasPermission(userService.getCurrentUser().getUserId(),
                PermissionType.RELEASE_NAMESPACE,
                RoleUtils.buildNamespaceTargetId(appId, namespaceName));
    }

    public boolean hasCreateApplicationPermission() {
        return rolePermissionService.userHasPermission(userService.getCurrentUser().getUserId(), PermissionType.CREATE_APPLICATION, null);
    }

    public boolean hasCreateClusterPermission(String appId) {
        return rolePermissionService.userHasPermission(userService.getCurrentUser().getUserId(),
                PermissionType.CREATE_CLUSTER,
                appId);
    }


}