package lucky.apollo.service;

import lucky.apollo.entity.po.AppPO;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
public interface RoleInitializationService {
    /**
     * 初始化角色
     *
     * @param app
     */
    void initAppRoles(AppPO app);
}