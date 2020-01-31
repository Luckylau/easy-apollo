package lucky.apollo.adminservice.controller;

import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.entity.dto.ClusterDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.exception.NotFoundException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.entity.ClusterPO;
import lucky.apollo.core.service.ClusterService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/10/15
 */
@RestController
public class ClusterController {

    private final ClusterService clusterService;

    public ClusterController(final ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @PostMapping("/apps/{appId}/clusters")
    public ClusterDTO create(@PathVariable("appId") String appId,
                             @RequestParam(value = "autoCreatePrivateNamespace", defaultValue = "true") boolean autoCreatePrivateNamespace,
                             @Valid @RequestBody ClusterDTO dto) {
        ClusterPO entity = BeanUtils.transformWithIgnoreNull(ClusterPO.class, dto);
        ClusterPO managedEntity = clusterService.findOne(appId, entity.getName());
        if (managedEntity != null) {
            throw new BadRequestException("cluster already exist.");
        }

        if (autoCreatePrivateNamespace) {
            entity = clusterService.saveWithInstanceOfAppNamespaces(entity);
        }

        return BeanUtils.transformWithIgnoreNull(ClusterDTO.class, entity);
    }

    @DeleteMapping("/apps/{appId}/clusters/{clusterName:.+}")
    public void delete(@PathVariable("appId") String appId,
                       @PathVariable("clusterName") String clusterName, @RequestParam String operator) {

        ClusterPO entity = clusterService.findOne(appId, clusterName);

        if (entity == null) {
            throw new NotFoundException("cluster not found for clusterName " + clusterName);
        }

        if (ConfigConsts.CLUSTER_NAME_DEFAULT.equals(entity.getName())) {
            throw new BadRequestException("can not delete default cluster!");
        }

        clusterService.delete(entity.getId(), operator);
    }

    @GetMapping("/apps/{appId}/clusters")
    public List<ClusterDTO> find(@PathVariable("appId") String appId) {
        List<ClusterPO> clusters = clusterService.findParentClusters(appId);
        return BeanUtils.batchTransformWithIgnoreNull(ClusterDTO.class, clusters);
    }

    @GetMapping("/apps/{appId}/clusters/{clusterName:.+}")
    public ClusterDTO get(@PathVariable("appId") String appId,
                          @PathVariable("clusterName") String clusterName) {
        ClusterPO cluster = clusterService.findOne(appId, clusterName);
        if (cluster == null) {
            throw new NotFoundException("cluster not found for name " + clusterName);
        }
        return BeanUtils.transformWithIgnoreNull(ClusterDTO.class, cluster);
    }

    @GetMapping("/apps/{appId}/cluster/{clusterName}/unique")
    public boolean isAppIdUnique(@PathVariable("appId") String appId,
                                 @PathVariable("clusterName") String clusterName) {
        return clusterService.isClusterNameUnique(appId, clusterName);
    }

}
