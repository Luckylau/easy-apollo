package lucky.apollo.service;

import lucky.apollo.entity.bo.UserInfo;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
public interface UserService {
    UserInfo findByusername(String userId);
}
