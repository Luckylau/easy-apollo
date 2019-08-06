package lucky.apollo.portal.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.portal.config.PortalConfig;
import lucky.apollo.portal.entity.bo.UserInfo;
import lucky.apollo.portal.entity.po.PermissionPO;
import lucky.apollo.portal.entity.po.RolePO;
import lucky.apollo.portal.entity.po.RolePermissionPO;
import lucky.apollo.portal.entity.po.UserRolePO;
import lucky.apollo.portal.repository.*;
import lucky.apollo.portal.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
@Service
@Slf4j
public class RolePermissionServiceImpl implements RolePermissionService {

    @Autowired
    private PortalConfig portalConfig;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private ConsumerRoleRepository consumerRoleRepository;

    @Override
    public boolean isSuperAdmin(String userId) {
        return portalConfig.superAdmins().contains(userId);
    }

    @Override
    public List<RolePO> findUserRoles(String userId) {
        List<UserRolePO> userRoles = userRoleRepository.findByUserId(userId);
        if (CollectionUtils.isEmpty(userRoles)) {
            return Collections.emptyList();
        }

        Set<Long> roleIds = userRoles.stream().map(UserRolePO::getRoleId).collect(Collectors.toSet());

        return Lists.newLinkedList(roleRepository.findAllById(roleIds));
    }

    @Override
    public RolePO findRoleByRoleName(String roleName) {
        return roleRepository.findTopByRoleName(roleName);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteRolePermissionsByAppIdAndNamespace(String appId, String namespaceName, String operator) {
        List<Long> permissionIds = permissionRepository.findPermissionIdsByAppIdAndNamespace(appId, namespaceName);

        if (!permissionIds.isEmpty()) {
            // 1. delete Permission
            permissionRepository.batchDelete(permissionIds, operator);

            // 2. delete Role Permission
            rolePermissionRepository.batchDeleteByPermissionIds(permissionIds, operator);
        }

        List<Long> roleIds = roleRepository.findRoleIdsByAppIdAndNamespace(appId, namespaceName);

        if (!roleIds.isEmpty()) {
            // 3. delete Role
            roleRepository.batchDelete(roleIds, operator);

            // 4. delete User Role
            userRoleRepository.batchDeleteByRoleIds(roleIds, operator);

            // 5. delete Consumer Role
            consumerRoleRepository.batchDeleteByRoleIds(roleIds, operator);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteRolePermissionsByAppId(String appId, String operator) {
        List<Long> permissionIds = permissionRepository.findPermissionIdsByAppId(appId);

        if (!permissionIds.isEmpty()) {
            // 1. delete Permission
            permissionRepository.batchDelete(permissionIds, operator);

            // 2. delete Role Permission
            rolePermissionRepository.batchDeleteByPermissionIds(permissionIds, operator);
        }

        List<Long> roleIds = roleRepository.findRoleIdsByAppId(appId);

        if (!roleIds.isEmpty()) {
            // 3. delete Role
            roleRepository.batchDelete(roleIds, operator);

            // 4. delete User Role
            userRoleRepository.batchDeleteByRoleIds(roleIds, operator);

            // 5. delete Consumer Role
            consumerRoleRepository.batchDeleteByRoleIds(roleIds, operator);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RolePO createRoleWithPermissions(RolePO role, Set<Long> permissionIds) {
        //roleName格式 ：RoleType+AppId
        RolePO current = findRoleByRoleName(role.getRoleName());
        Preconditions.checkState(current == null, "Role %s already exists!", role.getRoleName());

        RolePO createdRole = roleRepository.save(role);

        if (!CollectionUtils.isEmpty(permissionIds)) {
            Iterable<RolePermissionPO> rolePermissions = permissionIds.stream().map(permissionId -> {
                RolePermissionPO rolePermission = new RolePermissionPO();
                rolePermission.setRoleId(createdRole.getId());
                rolePermission.setPermissionId(permissionId);
                rolePermission.setDataChangeCreatedBy(createdRole.getDataChangeCreatedBy());
                rolePermission.setDataChangeLastModifiedBy(createdRole.getDataChangeLastModifiedBy());
                return rolePermission;
            }).collect(Collectors.toList());
            rolePermissionRepository.saveAll(rolePermissions);
        }

        return createdRole;
    }

    @Override
    public void removeRoleFromUsers(String roleName, Set<String> userIds, String operatorUserId) {

    }

    @Override
    public Set<UserInfo> queryUsersWithRole(String roleName) {
        return null;
    }

    @Override
    public boolean userHasPermission(String userId, String permissionType, String targetId) {
        PermissionPO permission =
                permissionRepository.findTopByPermissionTypeAndTargetId(permissionType, targetId);
        if (permission == null) {
            return false;
        }

        if (isSuperAdmin(userId)) {
            return true;
        }

        List<UserRolePO> userRoles = userRoleRepository.findByUserId(userId);
        if (CollectionUtils.isEmpty(userRoles)) {
            return false;
        }

        Set<Long> roleIds =
                userRoles.stream().map(UserRolePO::getRoleId).collect(Collectors.toSet());
        List<RolePermissionPO> rolePermissions = rolePermissionRepository.findByRoleIdIn(roleIds);
        if (CollectionUtils.isEmpty(rolePermissions)) {
            return false;
        }

        for (RolePermissionPO rolePermission : rolePermissions) {
            if (rolePermission.getPermissionId() == permission.getId()) {
                return true;
            }
        }

        return false;
    }

    @Transactional
    @Override
    public PermissionPO createPermission(PermissionPO permission) {
        String permissionType = permission.getPermissionType();
        String targetId = permission.getTargetId();
        PermissionPO current =
                permissionRepository.findTopByPermissionTypeAndTargetId(permissionType, targetId);
        Preconditions.checkState(current == null,
                "Permission with permissionType %s targetId %s already exists!", permissionType, targetId);

        return permissionRepository.save(permission);
    }


    @Override
    public Set<PermissionPO> createPermissions(Set<PermissionPO> permissions) {
        Multimap<String, String> targetIdPermissionTypes = HashMultimap.create();
        for (PermissionPO permissionPO : permissions) {
            targetIdPermissionTypes.put(permissionPO.getTargetId(), permissionPO.getPermissionType());
        }

        for (String targetId : targetIdPermissionTypes.keySet()) {
            Collection<String> permissionTypes = targetIdPermissionTypes.get(targetId);
            List<PermissionPO> current = permissionRepository.findByPermissionTypeInAndTargetId(permissionTypes, targetId);
            Preconditions.checkState(CollectionUtils.isEmpty(current), "Permission with permissionType %s targetId %s already exists!", permissionTypes, targetId);
        }

        Iterable<PermissionPO> results = permissionRepository.saveAll(permissions);
        return FluentIterable.from(results).toSet();
    }


    @Override
    public Set<String> assignRoleToUsers(String roleName, Set<String> userIds, String operatorUserId) {
        RolePO role = findRoleByRoleName(roleName);
        Preconditions.checkState(role != null, "Role %s doesn't exist!", roleName);

        List<UserRolePO> existedUserRoles =
                userRoleRepository.findByUserIdInAndRoleId(userIds, role.getId());
        Set<String> existedUserIds =
                existedUserRoles.stream().map(UserRolePO::getUserId).collect(Collectors.toSet());

        Set<String> toAssignUserIds = Sets.difference(userIds, existedUserIds);

        Iterable<UserRolePO> toCreate = toAssignUserIds.stream().map(userId -> {
            UserRolePO userRole = new UserRolePO();
            userRole.setRoleId(role.getId());
            userRole.setUserId(userId);
            userRole.setDataChangeCreatedBy(operatorUserId);
            userRole.setDataChangeLastModifiedBy(operatorUserId);
            return userRole;
        }).collect(Collectors.toList());

        userRoleRepository.saveAll(toCreate);
        return toAssignUserIds;
    }
}