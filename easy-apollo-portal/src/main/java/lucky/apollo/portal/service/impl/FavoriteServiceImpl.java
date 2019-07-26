package lucky.apollo.portal.service.impl;


import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.portal.entity.bo.UserInfo;
import lucky.apollo.portal.entity.po.FavoritePO;
import lucky.apollo.portal.repository.FavoriteRepository;
import lucky.apollo.portal.service.FavoriteService;
import lucky.apollo.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    public static final long POSITION_DEFAULT = 10000;

    @Autowired
    private UserService userService;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Override
    public FavoritePO addFavorite(FavoritePO favorite) {
        UserInfo user = userService.findByusername(favorite.getUserId());
        if (user == null) {
            throw new BadRequestException("user not exist");
        }

        UserInfo loginUser = userService.getCurrentUser();
        //user can only add himself favorite app
        if (!loginUser.equals(user)) {
            throw new BadRequestException("add favorite fail. "
                    + "because favorite's user is not current login user.");
        }

        FavoritePO checkedFavorite = favoriteRepository.findByUserIdAndAppId(loginUser.getUserId(), favorite.getAppId());
        if (checkedFavorite != null) {
            return checkedFavorite;
        }

        favorite.setPosition(POSITION_DEFAULT);
        favorite.setDataChangeCreatedBy(user.getUserId());
        favorite.setDataChangeLastModifiedBy(user.getUserId());

        return favoriteRepository.save(favorite);
    }

    @Override
    public List<FavoritePO> search(String userId, String appId, Pageable page) {
        boolean isUserIdEmpty = StringUtils.isEmpty(userId);
        boolean isAppIdEmpty = StringUtils.isEmpty(appId);

        if (isAppIdEmpty && isUserIdEmpty) {
            throw new BadRequestException("user id and app id can't be empty at the same time");
        }

        if (!isAppIdEmpty && !isUserIdEmpty) {
            //search by userId and appId
            return Arrays.asList(favoriteRepository.findByUserIdAndAppId(userId, appId));
        }

        //search by userId
        if (!isUserIdEmpty) {
            return favoriteRepository.findByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(userId, page);
        } else {
            //search by appId
            return favoriteRepository.findByAppIdOrderByPositionAscDataChangeCreatedTimeAsc(appId, page);
        }
    }

    @Override
    public void deleteFavorite(long favoriteId) {
        FavoritePO favorite = favoriteRepository.findById(favoriteId).orElse(null);

        checkUserOperatePermission(favorite);

        favoriteRepository.delete(favorite);
    }

    @Override
    public void adjustFavoriteToFirst(long favoriteId) {
        FavoritePO favorite = favoriteRepository.findById(favoriteId).orElse(null);

        checkUserOperatePermission(favorite);

        String userId = favorite.getUserId();
        FavoritePO firstFavorite = favoriteRepository.findFirstByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(userId);
        long minPosition = firstFavorite.getPosition();

        favorite.setPosition(minPosition - 1);

        favoriteRepository.save(favorite);
    }

    private void checkUserOperatePermission(FavoritePO favorite) {
        if (favorite == null) {
            throw new BadRequestException("favorite not exist");
        }

        if (!Objects.equals(userService.getCurrentUser().getUserId(), favorite.getUserId())) {
            throw new BadRequestException("can not operate other person's favorite");
        }
    }

    @Override
    public void batchDeleteByAppId(String appId, String operator) {
        favoriteRepository.batchDeleteByAppId(appId, operator);
    }
}