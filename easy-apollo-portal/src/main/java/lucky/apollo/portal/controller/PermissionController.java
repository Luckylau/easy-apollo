package lucky.apollo.portal.controller;

import com.google.common.collect.Sets;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.RequestPrecondition;
import lucky.apollo.portal.constant.PermissionType;
import lucky.apollo.portal.constant.RoleType;
import lucky.apollo.portal.entity.bo.UserInfo;
import lucky.apollo.portal.entity.vo.AppRolesAssignedUsers;
import lucky.apollo.portal.entity.vo.NamespaceRolesAssignedUsers;
import lucky.apollo.portal.entity.vo.PermissionCondition;
import lucky.apollo.portal.resolver.PermissionValidator;
import lucky.apollo.portal.service.RoleInitializationService;
import lucky.apollo.portal.service.RolePermissionService;
import lucky.apollo.portal.service.UserService;
import lucky.apollo.portal.utils.RoleUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@RestController
public class PermissionController {
    private final RolePermissionService rolePermissionService;
    private final UserService userService;
    private final RoleInitializationService roleInitializationService;
    private final PermissionValidator permissionValidator;


    public PermissionController(RolePermissionService rolePermissionService, UserService userService, RoleInitializationService roleInitializationService, PermissionValidator permissionValidator) {
        this.rolePermissionService = rolePermissionService;
        this.userService = userService;
        this.roleInitializationService = roleInitializationService;
        this.permissionValidator = permissionValidator;
    }

    @GetMapping("/apps/{appId}/permissions/{permissionType}")
    public ResponseEntity<PermissionCondition> hasPermission(@PathVariable String appId, @PathVariable String permissionType) {
        PermissionCondition permissionCondition = new PermissionCondition();

        permissionCondition.setHasPermission(
                rolePermissionService.userHasPermission(userService.getCurrentUser().getUserId(), permissionType, appId));

        return ResponseEntity.ok().body(permissionCondition);
    }

    @GetMapping("/apps/{appId}/namespaces/{namespaceName}/permissions/{permissionType}")
    public ResponseEntity<PermissionCondition> hasPermission(@PathVariable String appId, @PathVariable String namespaceName,
                                                             @PathVariable String permissionType) {
        PermissionCondition permissionCondition = new PermissionCondition();

        permissionCondition.setHasPermission(
                rolePermissionService.userHasPermission(userService.getCurrentUser().getUserId(), permissionType,
                        RoleUtils.buildNamespaceTargetId(appId, namespaceName)));

        return ResponseEntity.ok().body(permissionCondition);
    }

    @GetMapping("/apps/{appId}/envs/{env}/namespaces/{namespaceName}/permissions/{permissionType}")
    public ResponseEntity<PermissionCondition> hasPermission(@PathVariable String appId, @PathVariable String env, @PathVariable String namespaceName,
                                                             @PathVariable String permissionType) {
        PermissionCondition permissionCondition = new PermissionCondition();

        permissionCondition.setHasPermission(
                rolePermissionService.userHasPermission(userService.getCurrentUser().getUserId(), permissionType,
                        RoleUtils.buildNamespaceTargetId(appId, namespaceName)));

        return ResponseEntity.ok().body(permissionCondition);
    }

    @GetMapping("/permissions/root")
    public ResponseEntity<PermissionCondition> hasRootPermission() {
        PermissionCondition permissionCondition = new PermissionCondition();

        permissionCondition.setHasPermission(rolePermissionService.isSuperAdmin(userService.getCurrentUser().getUserId()));

        return ResponseEntity.ok().body(permissionCondition);
    }

    @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
    @DeleteMapping("/apps/{appId}/envs/{env}/namespaces/{namespaceName}/roles/{roleType}")
    public ResponseEntity<Void> removeNamespaceEnvRoleFromUser(@PathVariable String appId, @PathVariable String env, @PathVariable String namespaceName,
                                                               @PathVariable String roleType, @RequestParam String user) {
        RequestPrecondition.checkArgumentsNotEmpty(user);

        if (!RoleType.isValidRoleType(roleType)) {
            throw new BadRequestException("role type is illegal");
        }

        rolePermissionService.removeRoleFromUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType),
                Sets.newHashSet(user), userService.getCurrentUser().getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/apps/{appId}/namespaces/{namespaceName}/role_users")
    public NamespaceRolesAssignedUsers getNamespaceRoles(@PathVariable String appId, @PathVariable String namespaceName) {

        NamespaceRolesAssignedUsers assignedUsers = new NamespaceRolesAssignedUsers();
        assignedUsers.setNamespaceName(namespaceName);
        assignedUsers.setAppId(appId);

        Set<UserInfo> releaseNamespaceUsers =
                rolePermissionService.queryUsersWithRole(RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName));
        assignedUsers.setReleaseRoleUsers(releaseNamespaceUsers);

        Set<UserInfo> modifyNamespaceUsers =
                rolePermissionService.queryUsersWithRole(RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName));
        assignedUsers.setModifyRoleUsers(modifyNamespaceUsers);

        return assignedUsers;
    }

    @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
    @PostMapping("/apps/{appId}/namespaces/{namespaceName}/roles/{roleType}")
    public ResponseEntity<Void> assignNamespaceRoleToUser(@PathVariable String appId, @PathVariable String namespaceName,
                                                          @PathVariable String roleType, @RequestBody String user) {
        checkUserExists(user);
        RequestPrecondition.checkArgumentsNotEmpty(user);

        if (!RoleType.isValidRoleType(roleType)) {
            throw new BadRequestException("role type is illegal");
        }
        Set<String> assignedUser = rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType),
                Sets.newHashSet(user), userService.getCurrentUser().getUserId());
        if (CollectionUtils.isEmpty(assignedUser)) {
            throw new BadRequestException(user + "已授权");
        }

        return ResponseEntity.ok().build();
    }

    @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
    @DeleteMapping("/apps/{appId}/namespaces/{namespaceName}/roles/{roleType}")
    public ResponseEntity<Void> removeNamespaceRoleFromUser(@PathVariable String appId, @PathVariable String namespaceName,
                                                            @PathVariable String roleType, @RequestParam String user) {
        RequestPrecondition.checkArgumentsNotEmpty(user);

        if (!RoleType.isValidRoleType(roleType)) {
            throw new BadRequestException("role type is illegal");
        }
        rolePermissionService.removeRoleFromUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType),
                Sets.newHashSet(user), userService.getCurrentUser().getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/apps/{appId}/role_users")
    public AppRolesAssignedUsers getAppRoles(@PathVariable String appId) {
        AppRolesAssignedUsers users = new AppRolesAssignedUsers();
        users.setAppId(appId);

        Set<UserInfo> masterUsers = rolePermissionService.queryUsersWithRole(RoleUtils.buildAppMasterRoleName(appId));
        users.setMasterUsers(masterUsers);

        return users;
    }

    @PreAuthorize(value = "@permissionValidator.hasManageAppMasterPermission(#appId)")
    @PostMapping("/apps/{appId}/roles/{roleType}")
    public ResponseEntity<Void> assignAppRoleToUser(@PathVariable String appId, @PathVariable String roleType,
                                                    @RequestBody String user) {
        checkUserExists(user);
        RequestPrecondition.checkArgumentsNotEmpty(user);

        if (!RoleType.isValidRoleType(roleType)) {
            throw new BadRequestException("role type is illegal");
        }
        Set<String> assignedUsers = rolePermissionService.assignRoleToUsers(RoleUtils.buildAppRoleName(appId, roleType),
                Sets.newHashSet(user), userService.getCurrentUser().getUserId());
        if (CollectionUtils.isEmpty(assignedUsers)) {
            throw new BadRequestException(user + "已授权");
        }

        return ResponseEntity.ok().build();
    }

    @PreAuthorize(value = "@permissionValidator.hasManageAppMasterPermission(#appId)")
    @DeleteMapping("/apps/{appId}/roles/{roleType}")
    public ResponseEntity<Void> removeAppRoleFromUser(@PathVariable String appId, @PathVariable String roleType,
                                                      @RequestParam String user) {
        RequestPrecondition.checkArgumentsNotEmpty(user);

        if (!RoleType.isValidRoleType(roleType)) {
            throw new BadRequestException("role type is illegal");
        }
        rolePermissionService.removeRoleFromUsers(RoleUtils.buildAppRoleName(appId, roleType),
                Sets.newHashSet(user), userService.getCurrentUser().getUserId());
        return ResponseEntity.ok().build();
    }

    private void checkUserExists(String userId) {
        if (userService.findByusername(userId) == null) {
            throw new BadRequestException(String.format("User %s does not exist!", userId));
        }
    }

    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @PostMapping("/apps/{appId}/system/master/{userId}")
    public ResponseEntity<Void> addManageAppMasterRoleToUser(@PathVariable String appId, @PathVariable String userId) {
        checkUserExists(userId);
        roleInitializationService.initManageAppMasterRole(appId, userService.getCurrentUser().getUserId());
        Set<String> userIds = new HashSet<>();
        userIds.add(userId);
        rolePermissionService.assignRoleToUsers(RoleUtils.buildManageAppMasterRoleName(PermissionType.MANAGE_APP_MASTER, appId),
                userIds, userService.getCurrentUser().getUserId());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @DeleteMapping("/apps/{appId}/system/master/{userId}")
    public ResponseEntity<Void> forbidManageAppMaster(@PathVariable String appId, @PathVariable String userId) {
        checkUserExists(userId);
        roleInitializationService.initManageAppMasterRole(appId, userService.getCurrentUser().getUserId());
        Set<String> userIds = new HashSet<>();
        userIds.add(userId);
        rolePermissionService.removeRoleFromUsers(RoleUtils.buildManageAppMasterRoleName(PermissionType.MANAGE_APP_MASTER, appId),
                userIds, userService.getCurrentUser().getUserId());
        return ResponseEntity.ok().build();
    }
}