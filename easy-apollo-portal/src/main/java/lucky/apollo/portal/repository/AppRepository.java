package lucky.apollo.portal.repository;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */


import lucky.apollo.common.entity.po.AppPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Set;

public interface AppRepository extends PagingAndSortingRepository<AppPO, Long> {

    AppPO findByAppId(String appId);

    List<AppPO> findByOwnerName(String ownerName, Pageable page);

    List<AppPO> findByAppIdIn(Set<String> appIds);

    List<AppPO> findByAppIdIn(Set<String> appIds, Pageable pageable);

    Page<AppPO> findByAppIdContainingOrNameContaining(String appId, String name, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE App SET IsDeleted=1,DataChange_LastModifiedBy = ?2 WHERE AppId=?1", nativeQuery = true)
    int deleteApp(String appId, String operator);
}