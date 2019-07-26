package lucky.apollo.portal.service;


import lucky.apollo.common.entity.dto.NamespaceDTO;
import lucky.apollo.portal.entity.bo.NamespaceInfo;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
public interface NamespaceService {
    NamespaceDTO createNamespace(NamespaceDTO namespace);

    void deleteNamespace(String appId, String namespaceName);

    NamespaceDTO loadNamespaceBaseInfo(String appId, String namespaceName);

    List<NamespaceInfo> findNamespaceBOs(String appId);

    List<NamespaceDTO> findNamespaces(String appId);

    NamespaceInfo loadNamespaceBO(String appId, String namespaceName);

    boolean namespaceHasInstances(String appId, String namespaceName);

    void assignNamespaceRoleToOperator(String appId, String namespaceName, String operator);
}