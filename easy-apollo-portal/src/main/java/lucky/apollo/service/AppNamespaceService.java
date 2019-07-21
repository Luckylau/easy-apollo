package lucky.apollo.service;

import lucky.apollo.entity.po.AppNamespacePO;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
public interface AppNamespaceService {
    /**
     * 创建默认
     *
     * @param appId
     */
    void createDefaultAppNamespace(String appId);

    /**
     * 删除
     *
     * @param appId
     * @param operator
     */
    void batchDeleteByAppId(String appId, String operator);

    /**
     * 查找
     *
     * @param appId
     * @param namespaceName
     * @return
     */
    AppNamespacePO findByAppIdAndName(String appId, String namespaceName);


}