package lucky.apollo.portal.controller;

import lucky.apollo.common.entity.dto.ClusterDTO;
import lucky.apollo.portal.service.ClusterService;
import lucky.apollo.portal.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author luckylau
 * @Date 2019/10/12
 */
@RestController
public class ClusterController {
    private final ClusterService clusterService;
    private final UserService userService;

    public ClusterController(ClusterService clusterService, UserService userService) {
        this.clusterService = clusterService;
        this.userService = userService;
    }

    @PreAuthorize(value = "@permissionValidator.hasCreateClusterPermission(#appId)")
    @PostMapping(value = "apps/{appId}/envs/{env}/clusters")
    public ClusterDTO createCluster(@PathVariable String appId, @PathVariable String env,
                                    @Valid @RequestBody ClusterDTO cluster) {
        String operator = userService.getCurrentUser().getUserId();
        cluster.setDataChangeLastModifiedBy(operator);
        cluster.setDataChangeCreatedBy(operator);

        return clusterService.createCluster(cluster);
    }

    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @DeleteMapping(value = "apps/{appId}/envs/{env}/clusters/{clusterName:.+}")
    public ResponseEntity<Void> deleteCluster(@PathVariable String appId, @PathVariable String env,
                                              @PathVariable String clusterName) {
        clusterService.deleteCluster(appId, clusterName);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "apps/{appId}/envs/{env}/clusters/{clusterName:.+}")
    public ClusterDTO loadCluster(@PathVariable("appId") String appId, @PathVariable String env, @PathVariable("clusterName") String clusterName) {

        return clusterService.loadCluster(appId, clusterName);
    }

}
