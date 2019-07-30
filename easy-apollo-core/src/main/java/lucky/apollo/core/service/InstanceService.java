package lucky.apollo.core.service;

import lucky.apollo.core.entity.InstanceConfigPO;
import lucky.apollo.core.entity.InstancePO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
public interface InstanceService {
    InstancePO findInstance(String appId, String clusterName, String dataCenter, String ip);

    List<InstancePO> findInstancesByIds(Set<Long> instanceIds);

    InstancePO createInstance(InstancePO instance);

    InstanceConfigPO findInstanceConfig(long instanceId, String configAppId, String
            configNamespaceName);

    Page<InstanceConfigPO> findActiveInstanceConfigsByReleaseKey(String releaseKey, Pageable
            pageable);

    Page<InstancePO> findInstancesByNamespace(String appId, String clusterName, String
            namespaceName, Pageable pageable);

    Page<InstancePO> findInstancesByNamespaceAndInstanceAppId(String instanceAppId, String
            appId, String clusterName, String
                                                                      namespaceName, Pageable
                                                                      pageable);

    List<InstanceConfigPO> findInstanceConfigsByNamespaceWithReleaseKeysNotIn(String appId,
                                                                              String clusterName,
                                                                              String
                                                                                      namespaceName,
                                                                              Set<String>
                                                                                      releaseKeysNotIn);

    InstanceConfigPO createInstanceConfig(InstanceConfigPO instanceConfig);

    InstanceConfigPO updateInstanceConfig(InstanceConfigPO instanceConfig);

    int batchDeleteInstanceConfig(String configAppId, String configClusterName, String configNamespaceName);
}
