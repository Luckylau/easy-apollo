package lucky.apollo.service;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
public interface FavoriteService {

    void batchDeleteByAppId(String appId, String operator);
}