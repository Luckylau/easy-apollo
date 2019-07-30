package lucky.apollo.core.repository;

import lucky.apollo.core.entity.ReleaseHistoryPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface ReleaseHistoryRepository extends PagingAndSortingRepository<ReleaseHistoryPO, Long> {
    Page<ReleaseHistoryPO> findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(String appId, String
            clusterName, String namespaceName, Pageable pageable);

    Page<ReleaseHistoryPO> findByReleaseIdAndOperationOrderByIdDesc(long releaseId, int operation, Pageable pageable);

    Page<ReleaseHistoryPO> findByPreviousReleaseIdAndOperationOrderByIdDesc(long previousReleaseId, int operation, Pageable pageable);

    Page<ReleaseHistoryPO> findByReleaseIdAndOperationInOrderByIdDesc(long releaseId, Set<Integer> operations, Pageable pageable);

    @Modifying
    @Query(value = "update ReleaseHistory set isdeleted=1,DataChange_LastModifiedBy = ?4 where appId=?1 and clusterName=?2 and namespaceName = ?3", nativeQuery = true)
    int batchDelete(String appId, String clusterName, String namespaceName, String operator);

}

