package lucky.apollo.portal.service;


import lucky.apollo.common.entity.po.AppPO;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
public interface RoleInitializationService {
    /**
     * 初始化角色
     * @param app
     */
    void initAppRoles(AppPO app);

    void initNamespaceRoles(String appId, String namespaceName, String operator);

    void initCreateAppRole();

    void initManageAppMasterRole(String appId, String operator);

}