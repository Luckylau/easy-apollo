package lucky.apollo.core.service;


import lucky.apollo.common.entity.po.AppNamespacePO;

import java.util.List;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface AppNamespaceService {
    boolean isAppNamespaceNameUnique(String appId, String namespaceName);

    AppNamespacePO findPublicNamespaceByName(String namespaceName);

    List<AppNamespacePO> findByAppId(String appId);

    List<AppNamespacePO> findPublicNamespacesByNames(Set<String> namespaceNames);


    AppNamespacePO findOne(String appId, String namespaceName);

    List<AppNamespacePO> findPrivateAppNamespace(String appId);

    List<AppNamespacePO> findByAppIdAndNamespaces(String appId, Set<String> namespaceNames);

    void createDefaultAppNamespace(String appId, String createBy);

    AppNamespacePO createAppNamespace(AppNamespacePO appNamespace);

    AppNamespacePO update(AppNamespacePO appNamespace);

    void batchDelete(String appId, String operator);

    void deleteAppNamespace(AppNamespacePO appNamespace, String operator);
}
