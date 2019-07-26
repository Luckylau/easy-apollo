package lucky.apollo.portal.service;


import lucky.apollo.common.entity.po.AppNamespacePO;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
public interface AppNamespaceService {
    /**
     * 创建默认
     * @param appId
     */
    void createDefaultAppNamespace(String appId);

    /**
     * 删除
     * @param appId
     * @param operator
     */
    void batchDeleteByAppId(String appId, String operator);

    /**
     * 查找
     * @param appId
     * @param namespaceName
     * @return
     */
    AppNamespacePO findByAppIdAndName(String appId, String namespaceName);


    AppNamespacePO deleteAppNamespace(String appId, String namespaceName);


    AppNamespacePO createAppNamespaceInLocal(AppNamespacePO appNamespace, boolean appendNamespacePrefix);

    List<AppNamespacePO> findByAppId(String appId);


}