package lucky.apollo.core.service;

import lucky.apollo.core.entity.NamespacePO;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface NamespaceService {
    NamespacePO findOne(Long namespaceId);

    NamespacePO findOne(String appId, String clusterName, String namespaceName);

    NamespacePO deleteNamespace(NamespacePO namespace, String operator);

    NamespacePO save(NamespacePO entity);

    NamespacePO update(NamespacePO namespace);

    void instanceOfAppNamespaces(String appId, String clusterName, String createBy);

    List<NamespacePO> findByAppIdAndNamespaceName(String appId, String namespaceName);
}

