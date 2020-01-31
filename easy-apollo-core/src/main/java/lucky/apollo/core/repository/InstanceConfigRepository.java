package lucky.apollo.core.repository;

import lucky.apollo.core.entity.InstanceConfigPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
public interface InstanceConfigRepository extends PagingAndSortingRepository<InstanceConfigPO, Long> {

    InstanceConfigPO findByInstanceIdAndConfigAppIdAndConfigNamespaceName(long instanceId, String
            configAppId, String configNamespaceName);

    Page<InstanceConfigPO> findByReleaseKeyAndDataChangeLastModifiedTimeAfter(String releaseKey, Date
            validDate, Pageable pageable);

    Page<InstanceConfigPO> findByConfigAppIdAndConfigClusterNameAndConfigNamespaceNameAndDataChangeLastModifiedTimeAfter(
            String appId, String clusterName, String namespaceName, Date validDate, Pageable pageable);

    List<InstanceConfigPO> findByConfigAppIdAndConfigClusterNameAndConfigNamespaceNameAndDataChangeLastModifiedTimeAfterAndReleaseKeyNotIn(
            String appId, String clusterName, String namespaceName, Date validDate, Set<String> releaseKey);

    @Modifying
    @Query(value = "delete from InstanceConfig  where ConfigAppId=?1 and ConfigClusterName=?2 and ConfigNamespaceName = ?3", nativeQuery = true)
    int batchDelete(String appId, String clusterName, String namespaceName);

    @Query(
            value = "select b.Id from `InstanceConfig` a inner join `Instance` b on b.Id =" +
                    " a.`InstanceId` where a.`ConfigAppId` = :configAppId and a.`ConfigClusterName` = " +
                    ":clusterName and a.`ConfigNamespaceName` = :namespaceName and a.`DataChange_LastTime` " +
                    "> :validDate and b.`AppId` = :instanceAppId",
            countQuery = "select count(1) from `InstanceConfig` a inner join `Instance` b on b.id =" +
                    " a.`InstanceId` where a.`ConfigAppId` = :configAppId and a.`ConfigClusterName` = " +
                    ":clusterName and a.`ConfigNamespaceName` = :namespaceName and a.`DataChange_LastTime` " +
                    "> :validDate and b.`AppId` = :instanceAppId",
            nativeQuery = true)
    Page<Object> findInstanceIdsByNamespaceAndInstanceAppId(
            @Param("instanceAppId") String instanceAppId, @Param("configAppId") String configAppId,
            @Param("clusterName") String clusterName, @Param("namespaceName") String namespaceName,
            @Param("validDate") Date validDate, Pageable pageable);
}
