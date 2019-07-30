package lucky.apollo.core.repository;

import lucky.apollo.core.entity.NamespacePO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface NamespaceRepository extends PagingAndSortingRepository<NamespacePO, Long> {

    List<NamespacePO> findByAppIdAndClusterNameOrderByIdAsc(String appId, String clusterName);

    NamespacePO findByAppIdAndClusterNameAndNamespaceName(String appId, String clusterName, String namespaceName);

    @Modifying
    @Query(value = "update Namespace set isdeleted=1,DataChange_LastModifiedBy = ?3 where appId=?1 and clusterName=?2", nativeQuery = true)
    int batchDelete(String appId, String clusterName, String operator);

    List<NamespacePO> findByAppIdAndNamespaceNameOrderByIdAsc(String appId, String namespaceName);

    List<NamespacePO> findByNamespaceName(String namespaceName, Pageable page);

    int countByNamespaceNameAndAppIdNot(String namespaceName, String appId);

}
