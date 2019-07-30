package lucky.apollo.core.repository;

import lucky.apollo.core.entity.CommitPO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface CommitRepository extends PagingAndSortingRepository<CommitPO, Long> {

    List<CommitPO> findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(String appId, String clusterName,
                                                                          String namespaceName, Pageable pageable);

    @Modifying
    @Query(value = "update Commit set isdeleted=1,DataChange_LastModifiedBy = ?4 where appId=?1 and clusterName=?2 and namespaceName = ?3", nativeQuery = true)
    int batchDelete(String appId, String clusterName, String namespaceName, String operator);

}

