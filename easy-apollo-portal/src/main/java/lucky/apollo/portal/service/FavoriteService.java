package lucky.apollo.portal.service;


import lucky.apollo.portal.entity.po.FavoritePO;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
public interface FavoriteService {

    void batchDeleteByAppId(String appId, String operator);

    FavoritePO addFavorite(FavoritePO favorite);

    List<FavoritePO> search(String userId, String appId, Pageable page);

    void deleteFavorite(long favoriteId);

    void adjustFavoriteToFirst(long favoriteId);
}