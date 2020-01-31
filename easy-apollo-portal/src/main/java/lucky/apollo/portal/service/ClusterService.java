package lucky.apollo.portal.service;

import lucky.apollo.common.entity.dto.ClusterDTO;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/10/12
 */
public interface ClusterService {

    List<ClusterDTO> findClusters(String appId);

    ClusterDTO createCluster(ClusterDTO cluster);

    void deleteCluster(String appId, String clusterName);

    ClusterDTO loadCluster(String appId, String clusterName);
}
