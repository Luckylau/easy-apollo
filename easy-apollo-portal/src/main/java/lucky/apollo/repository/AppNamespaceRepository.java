package lucky.apollo.repository;

import lucky.apollo.entity.po.AppNamespacePO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
public interface AppNamespaceRepository extends PagingAndSortingRepository<AppNamespacePO, Long> {

    AppNamespacePO findByAppIdAndName(String appId, String namespaceName);

    AppNamespacePO findByName(String namespaceName);

    List<AppNamespacePO> findByNameAndIsPublic(String namespaceName, boolean isPublic);

    List<AppNamespacePO> findByAppId(String appId);

    @Modifying
    @Query(value = "UPDATE AppNamespace SET IsDeleted=1,DataChange_LastModifiedBy=?2 WHERE AppId=?1", nativeQuery = true)
    int batchDeleteByAppId(String appId, String operator);

    @Modifying
    @Query(value = "UPDATE AppNamespace SET IsDeleted=1,DataChange_LastModifiedBy = ?3 WHERE AppId=?1 and Name = ?2", nativeQuery = true)
    int delete(String appId, String namespaceName, String operator);
}