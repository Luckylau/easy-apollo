package lucky.apollo.portal.service.impl;

import com.google.common.collect.Sets;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.entity.po.AppPO;
import lucky.apollo.common.entity.po.BasePO;
import lucky.apollo.portal.constant.PermissionType;
import lucky.apollo.portal.constant.RoleType;
import lucky.apollo.portal.entity.po.PermissionPO;
import lucky.apollo.portal.entity.po.RolePO;
import lucky.apollo.portal.service.RoleInitializationService;
import lucky.apollo.portal.service.RolePermissionService;
import lucky.apollo.portal.utils.RoleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
@Service
public class RoleInitializationServiceImpl implements RoleInitializationService {

    @Autowired
    private RolePermissionService rolePermissionService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void initAppRoles(AppPO app) {
        String appId = app.getAppId();

        String appMasterRoleName = RoleUtils.buildAppMasterRoleName(appId);

        //has created before
        if (rolePermissionService.findRoleByRoleName(appMasterRoleName) != null) {
            return;
        }
        String operator = app.getDataChangeCreatedBy();
        //create app permissions
        createAppMasterRole(appId, operator);
        //create manageAppMaster permission
        createManageAppMasterRole(appId, operator);

        //assign master role to user
        rolePermissionService
                .assignRoleToUsers(RoleUtils.buildAppMasterRoleName(appId), Sets.newHashSet(app.getOwnerName()),
                        operator);

        initNamespaceRoles(appId, ConfigConsts.NAMESPACE_APPLICATION, operator);

        //assign modify、release namespace role to user
        rolePermissionService.assignRoleToUsers(
                RoleUtils.buildNamespaceRoleName(appId, ConfigConsts.NAMESPACE_APPLICATION, RoleType.MODIFY_NAMESPACE),
                Sets.newHashSet(operator), operator);
        rolePermissionService.assignRoleToUsers(
                RoleUtils.buildNamespaceRoleName(appId, ConfigConsts.NAMESPACE_APPLICATION, RoleType.RELEASE_NAMESPACE),
                Sets.newHashSet(operator), operator);
    }

    private void createAppMasterRole(String appId, String operator) {
        Set<PermissionPO> appPermissions =
                Stream.of(PermissionType.CREATE_NAMESPACE, PermissionType.ASSIGN_ROLE)
                        .map(permissionType -> createPermission(appId, permissionType, operator)).collect(Collectors.toSet());
        Set<PermissionPO> createdAppPermissions = rolePermissionService.createPermissions(appPermissions);
        Set<Long>
                appPermissionIds =
                createdAppPermissions.stream().map(BasePO::getId).collect(Collectors.toSet());

        //create app master role
        RolePO appMasterRole = createRole(RoleUtils.buildAppMasterRoleName(appId), operator);

        rolePermissionService.createRoleWithPermissions(appMasterRole, appPermissionIds);
    }

    private PermissionPO createPermission(String targetId, String permissionType, String operator) {
        PermissionPO permission = new PermissionPO();
        permission.setPermissionType(permissionType);
        permission.setTargetId(targetId);
        permission.setDataChangeCreatedBy(operator);
        permission.setDataChangeLastModifiedBy(operator);
        return permission;
    }

    private RolePO createRole(String roleName, String operator) {
        RolePO role = new RolePO();
        role.setRoleName(roleName);
        role.setDataChangeCreatedBy(operator);
        role.setDataChangeLastModifiedBy(operator);
        return role;
    }

    private void createManageAppMasterRole(String appId, String operator) {
        PermissionPO permission = createPermission(appId, PermissionType.MANAGE_APP_MASTER, operator);
        rolePermissionService.createPermission(permission);
        RolePO role = createRole(RoleUtils.buildManageAppMasterRoleName(PermissionType.MANAGE_APP_MASTER, appId), operator);
        Set<Long> permissionIds = new HashSet<>();
        permissionIds.add(permission.getId());
        rolePermissionService.createRoleWithPermissions(role, permissionIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void initNamespaceRoles(String appId, String namespaceName, String operator) {
        String modifyNamespaceRoleName = RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName);
        if (rolePermissionService.findRoleByRoleName(modifyNamespaceRoleName) == null) {
            createNamespaceRole(appId, namespaceName, PermissionType.MODIFY_NAMESPACE,
                    modifyNamespaceRoleName, operator);
        }

        String releaseNamespaceRoleName = RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName);
        if (rolePermissionService.findRoleByRoleName(releaseNamespaceRoleName) == null) {
            createNamespaceRole(appId, namespaceName, PermissionType.RELEASE_NAMESPACE,
                    releaseNamespaceRoleName, operator);
        }
    }

    private void createNamespaceRole(String appId, String namespaceName, String permissionType,
                                     String roleName, String operator) {

        PermissionPO permission =
                createPermission(RoleUtils.buildNamespaceTargetId(appId, namespaceName), permissionType, operator);
        PermissionPO createdPermission = rolePermissionService.createPermission(permission);

        RolePO role = createRole(roleName, operator);
        rolePermissionService
                .createRoleWithPermissions(role, Sets.newHashSet(createdPermission.getId()));
    }

    @Override
    public void initCreateAppRole() {
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void initManageAppMasterRole(String appId, String operator) {
        String manageAppMasterRoleName = RoleUtils.buildManageAppMasterRoleName(PermissionType.MANAGE_APP_MASTER, appId);
        if (rolePermissionService.findRoleByRoleName(manageAppMasterRoleName) != null) {
            return;
        }
        synchronized (RoleInitializationServiceImpl.class) {
            createManageAppMasterRole(appId, operator);
        }
    }

}