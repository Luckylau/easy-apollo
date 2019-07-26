package lucky.apollo.portal.repository;


import lucky.apollo.portal.entity.po.RolePO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/15
 */
public interface RoleRepository extends PagingAndSortingRepository<RolePO, Long> {

    /**
     * find role by role name
     */
    RolePO findTopByRoleName(String roleName);

    @Query(value = "SELECT r.id from Role r where (r.roleName = CONCAT('Master+', ?1) "
            + "OR r.roleName like CONCAT('ModifyNamespace+', ?1, '+%') "
            + "OR r.roleName like CONCAT('ReleaseNamespace+', ?1, '+%')  "
            + "OR r.roleName = CONCAT('ManageAppMaster+', ?1))", nativeQuery = true)
    List<Long> findRoleIdsByAppId(String appId);

    @Query(value = "SELECT r.id from Role r where (r.roleName = CONCAT('ModifyNamespace+', ?1, '+', ?2) "
            + "OR r.roleName = CONCAT('ReleaseNamespace+', ?1, '+', ?2))", nativeQuery = true)
    List<Long> findRoleIdsByAppIdAndNamespace(String appId, String namespaceName);

    @Modifying
    @Query(value = "UPDATE Role SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE Id in ?1", nativeQuery = true)
    Integer batchDelete(List<Long> roleIds, String operator);
}