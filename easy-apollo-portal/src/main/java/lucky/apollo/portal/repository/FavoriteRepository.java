package lucky.apollo.portal.repository;


import lucky.apollo.portal.entity.po.FavoritePO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
public interface FavoriteRepository extends PagingAndSortingRepository<FavoritePO, Long> {

    List<FavoritePO> findByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(String userId, Pageable page);

    List<FavoritePO> findByAppIdOrderByPositionAscDataChangeCreatedTimeAsc(String appId, Pageable page);

    FavoritePO findFirstByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(String userId);

    FavoritePO findByUserIdAndAppId(String userId, String appId);

    @Modifying
    @Query(value = "UPDATE Favorite SET IsDeleted=1,DataChange_LastModifiedBy = ?2 WHERE AppId=?1", nativeQuery = true)
    int batchDeleteByAppId(String appId, String operator);
}