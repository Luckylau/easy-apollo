package lucky.apollo.core.repository;

import lucky.apollo.core.entity.ReleasePO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface ReleaseRepository extends PagingAndSortingRepository<ReleasePO, Long> {

    ReleasePO findFirstByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(@Param("appId") String appId, @Param("clusterName") String clusterName,
                                                                                             @Param("namespaceName") String namespaceName);

    ReleasePO findByIdAndIsAbandonedFalse(long id);

    List<ReleasePO> findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(String appId, String clusterName, String namespaceName, Pageable page);

    List<ReleasePO> findByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(String appId, String clusterName, String namespaceName, Pageable page);

    List<ReleasePO> findByReleaseKeyIn(Set<String> releaseKey);

    List<ReleasePO> findByIdIn(Set<Long> releaseIds);

    @Modifying
    @Query(value = "update Release set isdeleted=1,DataChange_LastModifiedBy = ?4 where appId=?1 and clusterName=?2 and namespaceName = ?3", nativeQuery = true)
    int batchDelete(String appId, String clusterName, String namespaceName, String operator);

    // For release history conversion program, need to delete after conversion it done
    List<ReleasePO> findByAppIdAndClusterNameAndNamespaceNameOrderByIdAsc(String appId, String clusterName, String namespaceName);
}