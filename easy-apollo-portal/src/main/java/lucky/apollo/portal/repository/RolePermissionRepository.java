package lucky.apollo.portal.repository;


import lucky.apollo.portal.entity.po.RolePermissionPO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/15
 */
public interface RolePermissionRepository extends PagingAndSortingRepository<RolePermissionPO, Long> {

    /**
     * find role permissions by role ids
     */
    List<RolePermissionPO> findByRoleIdIn(Collection<Long> roleId);

    @Modifying
    @Query(value = "UPDATE RolePermission SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE PermissionId in ?1", nativeQuery = true)
    Integer batchDeleteByPermissionIds(List<Long> permissionIds, String operator);
}