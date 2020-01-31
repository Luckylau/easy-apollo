package lucky.apollo.portal.controller;

import com.google.common.base.Splitter;
import lucky.apollo.common.entity.dto.InstanceDTO;
import lucky.apollo.common.entity.dto.PageDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.portal.adminsevice.api.AdminServiceApi;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@RestController
public class InstanceController {
    private static final Splitter RELEASES_SPLITTER = Splitter.on(",").omitEmptyStrings()
            .trimResults();

    private final AdminServiceApi adminServiceApi;

    public InstanceController(final AdminServiceApi adminServiceApi) {
        this.adminServiceApi = adminServiceApi;
    }

    @GetMapping("/envs/{env}/instances/by-release")
    public PageDTO<InstanceDTO> getByRelease(@PathVariable String env, @RequestParam long releaseId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {

        return adminServiceApi.getByRelease(releaseId, page, size);
    }

    @GetMapping("/envs/{env}/instances/by-namespace")
    public PageDTO<InstanceDTO> getByNamespace(@PathVariable String env, @RequestParam String appId,
                                               @RequestParam String clusterName, @RequestParam String namespaceName,
                                               @RequestParam(required = false) String instanceAppId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {

        return adminServiceApi.getByNamespace(appId, clusterName, namespaceName, instanceAppId, page, size);
    }

    @GetMapping("/envs/{env}/instances/by-namespace/count")
    public ResponseEntity<Integer> getInstanceCountByNamespace(@PathVariable String env, @RequestParam String appId,
                                                               @RequestParam String clusterName,
                                                               @RequestParam String namespaceName) {

        int count = adminServiceApi.getInstanceCountByNamespace(appId, namespaceName);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/envs/{env}/instances/by-namespace-and-releases-not-in")
    public List<InstanceDTO> getByReleasesNotIn(@PathVariable String env, @RequestParam String appId,
                                                @RequestParam String clusterName, @RequestParam String namespaceName,
                                                @RequestParam String releaseIds) {

        Set<Long> releaseIdSet = RELEASES_SPLITTER.splitToList(releaseIds).stream().map(Long::parseLong)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(releaseIdSet)) {
            throw new BadRequestException("release ids can not be empty");
        }

        return adminServiceApi.getByReleasesNotIn(appId, clusterName, namespaceName, releaseIdSet);
    }
}