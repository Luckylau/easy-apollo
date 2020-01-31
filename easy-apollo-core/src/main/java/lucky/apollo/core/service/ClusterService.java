package lucky.apollo.core.service;

import lucky.apollo.core.entity.ClusterPO;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/10/11
 */
public interface ClusterService {
    ClusterPO findOne(String appId, String name);

    ClusterPO findOne(long clusterId);

    List<ClusterPO> findParentClusters(String appId);

    ClusterPO saveWithInstanceOfAppNamespaces(ClusterPO entity);

    void delete(long id, String operator);

    ClusterPO update(ClusterPO cluster);

    void createDefaultCluster(String appId, String createBy);

    List<ClusterPO> findChildClusters(String appId, String parentClusterName);

    List<ClusterPO> findClusters(String appId);

    ClusterPO saveWithoutInstanceOfAppNamespaces(ClusterPO entity);

    boolean isClusterNameUnique(String appId, String clusterName);
}
