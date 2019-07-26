package lucky.apollo.portal.controller;

import lucky.apollo.portal.entity.po.FavoritePO;
import lucky.apollo.portal.service.FavoriteService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@RestController
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(final FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }


    @PostMapping("/favorites")
    public FavoritePO addFavorite(@RequestBody FavoritePO favorite) {
        return favoriteService.addFavorite(favorite);
    }


    @GetMapping("/favorites")
    public List<FavoritePO> findFavorites(@RequestParam(value = "userId", required = false) String userId,
                                          @RequestParam(value = "appId", required = false) String appId,
                                          Pageable page) {
        return favoriteService.search(userId, appId, page);
    }


    @DeleteMapping("/favorites/{favoriteId}")
    public void deleteFavorite(@PathVariable long favoriteId) {
        favoriteService.deleteFavorite(favoriteId);
    }


    @PutMapping("/favorites/{favoriteId}")
    public void toTop(@PathVariable long favoriteId) {
        favoriteService.adjustFavoriteToFirst(favoriteId);
    }
}