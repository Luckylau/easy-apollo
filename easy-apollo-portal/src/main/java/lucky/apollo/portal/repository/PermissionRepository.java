package lucky.apollo.portal.repository;


import lucky.apollo.portal.entity.po.PermissionPO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/15
 */
public interface PermissionRepository extends PagingAndSortingRepository<PermissionPO, Long> {
    /**
     * find permission by permission type and targetId
     */
    PermissionPO findTopByPermissionTypeAndTargetId(String permissionType, String targetId);

    /**
     * find permissions by permission types and targetId
     */
    List<PermissionPO> findByPermissionTypeInAndTargetId(Collection<String> permissionTypes,
                                                         String targetId);

    @Query(value = "SELECT p.id from Permission p where p.targetId = ?1 or p.targetId like CONCAT(?1, '+%')", nativeQuery = true)
    List<Long> findPermissionIdsByAppId(String appId);

    @Query(value = "SELECT p.id from Permission p where p.targetId = CONCAT(?1, '+', ?2)", nativeQuery = true)
    List<Long> findPermissionIdsByAppIdAndNamespace(String appId, String namespaceName);

    @Modifying
    @Query(value = "UPDATE Permission SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE Id in ?1", nativeQuery = true)
    Integer batchDelete(List<Long> permissionIds, String operator);
}