package lucky.apollo.portal.service.impl;

import lucky.apollo.common.entity.dto.ClusterDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.portal.adminsevice.api.AdminServiceApi;
import lucky.apollo.portal.service.ClusterService;
import lucky.apollo.portal.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/10/12
 */
@Service
public class ClusterServiceImpl implements ClusterService {
    private final UserService userService;

    private final AdminServiceApi adminServiceApi;


    public ClusterServiceImpl(UserService userService, AdminServiceApi adminServiceApi) {
        this.userService = userService;
        this.adminServiceApi = adminServiceApi;
    }

    @Override
    public List<ClusterDTO> findClusters(String appId) {
        return adminServiceApi.findClustersByApp(appId);
    }

    @Override
    public ClusterDTO createCluster(ClusterDTO cluster) {
        if (!adminServiceApi.isClusterUnique(cluster.getAppId(), cluster.getName())) {
            throw new BadRequestException(String.format("cluster %s already exists.", cluster.getName()));
        }
        return adminServiceApi.create(cluster);
    }

    @Override
    public void deleteCluster(String appId, String clusterName) {
        adminServiceApi.delete(appId, clusterName, userService.getCurrentUser().getUserId());
    }

    @Override
    public ClusterDTO loadCluster(String appId, String clusterName) {
        return adminServiceApi.loadCluster(appId, clusterName);
    }
}
