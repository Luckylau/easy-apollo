package lucky.apollo.portal.service;

import lucky.apollo.portal.entity.bo.UserInfo;
import lucky.apollo.portal.entity.po.PermissionPO;
import lucky.apollo.portal.entity.po.RolePO;

import java.util.List;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
public interface RolePermissionService {
    /**
     * 是否是超级管理员
     *
     * @param userId
     * @return
     */
    boolean isSuperAdmin(String userId);

    /**
     * 查询用户角色
     */
    List<RolePO> findUserRoles(String userId);

    /**
     * 分配角色
     *
     * @param roleName
     * @param userIds
     * @param operatorUserId
     * @return
     */
    Set<String> assignRoleToUsers(String roleName, Set<String> userIds,
                                  String operatorUserId);

    /**
     * 查找用户角色
     *
     * @param roleName
     * @return
     */
    RolePO findRoleByRoleName(String roleName);

    /**
     * 删除权限
     *
     * @param appId
     * @param namespaceName
     * @param operator
     */
    void deleteRolePermissionsByAppIdAndNamespace(String appId, String namespaceName, String operator);

    /**
     * delete permissions when delete app.
     */
    void deleteRolePermissionsByAppId(String appId, String operator);


    /**
     * Create role with permissions, note that role name should be unique
     */
    public RolePO createRoleWithPermissions(RolePO role, Set<Long> permissionIds);

    /**
     * Remove role from users
     */
    public void removeRoleFromUsers(String roleName, Set<String> userIds, String operatorUserId);

    /**
     * Query users with role
     */
    public Set<UserInfo> queryUsersWithRole(String roleName);

    /**
     * Check whether user has the permission
     */
    public boolean userHasPermission(String userId, String permissionType, String targetId);

    /**
     * Create permission, note that permissionType + targetId should be unique
     */
    public PermissionPO createPermission(PermissionPO permission);

    /**
     * Create permissions, note that permissionType + targetId should be unique
     */
    public Set<PermissionPO> createPermissions(Set<PermissionPO> permissions);

}