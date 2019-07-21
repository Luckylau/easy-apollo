package lucky.apollo.service;

import lucky.apollo.entity.bo.UserInfo;
import lucky.apollo.entity.po.UserPO;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
public interface UserService {
    /**
     * 根据名字查找
     *
     * @param username
     * @return
     */
    UserInfo findByusername(String username);

    /**
     * 如果存在就更新，如果不存在就创建
     *
     * @param user
     */
    void createOrUpdate(UserPO user);

    /**
     * 查找用户
     *
     * @param keyword
     * @param offset
     * @param limit
     * @return
     */
    List<UserInfo> searchUsers(String keyword, int offset, int limit);


    String getCurrentUsername();
}