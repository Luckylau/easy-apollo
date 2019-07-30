package lucky.apollo.core.repository;

import lucky.apollo.common.entity.po.AppNamespacePO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface AppNamespaceRepository extends PagingAndSortingRepository<AppNamespacePO, Long> {
    AppNamespacePO findByAppIdAndName(String appId, String namespaceName);

    List<AppNamespacePO> findByAppIdAndNameIn(String appId, Set<String> namespaceNames);

    List<AppNamespacePO> findByAppId(String appId);

    List<AppNamespacePO> findFirst500ByIdGreaterThanOrderByIdAsc(long id);

    @Modifying
    @Query(value = "UPDATE AppNamespace SET IsDeleted=1,DataChange_LastModifiedBy = ?2 WHERE AppId=?1", nativeQuery = true)
    int batchDeleteByAppId(String appId, String operator);

    @Modifying
    @Query(value = "UPDATE AppNamespace SET IsDeleted=1,DataChange_LastModifiedBy = ?3 WHERE AppId=?1 and Name = ?2", nativeQuery = true)
    int delete(String appId, String namespaceName, String operator);
}
