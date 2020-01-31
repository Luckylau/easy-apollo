package lucky.apollo.portal.controller;

import lucky.apollo.common.entity.dto.ReleaseDTO;
import lucky.apollo.common.exception.NotFoundException;
import lucky.apollo.portal.entity.bo.ReleaseBO;
import lucky.apollo.portal.entity.model.NamespaceReleaseModel;
import lucky.apollo.portal.entity.vo.ReleaseCompareResult;
import lucky.apollo.portal.listener.ConfigPublishEvent;
import lucky.apollo.portal.resolver.PermissionValidator;
import lucky.apollo.portal.service.ReleaseService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@Validated
@RestController
public class ReleaseController {

    private final ReleaseService releaseService;
    private final ApplicationEventPublisher publisher;
    private final PermissionValidator permissionValidator;


    public ReleaseController(ReleaseService releaseService, ApplicationEventPublisher publisher, PermissionValidator permissionValidator) {
        this.releaseService = releaseService;
        this.publisher = publisher;
        this.permissionValidator = permissionValidator;
    }

    @PreAuthorize(value = "@permissionValidator.hasReleaseNamespacePermission(#appId, #namespaceName)")
    @PostMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases")
    public ReleaseDTO createRelease(@PathVariable String appId,
                                    @PathVariable String env, @PathVariable String clusterName,
                                    @PathVariable String namespaceName, @RequestBody NamespaceReleaseModel model) {
        model.setAppId(appId);
        model.setNamespaceName(namespaceName);
        model.setClusterName(clusterName);

        ReleaseDTO createdRelease = releaseService.publish(model);

        ConfigPublishEvent event = ConfigPublishEvent.instance();
        event.withAppId(appId)
                .withNamespace(namespaceName)
                .withReleaseId(createdRelease.getId())
                .setNormalPublishEvent(true);
        publisher.publishEvent(event);

        return createdRelease;
    }

    @PreAuthorize(value = "@permissionValidator.hasReleaseNamespacePermission(#appId, #namespaceName)")
    @PostMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/releases")
    public ReleaseDTO createGrayRelease(@PathVariable String appId,
                                        @PathVariable String env, @PathVariable String clusterName,
                                        @PathVariable String namespaceName, @PathVariable String branchName,
                                        @RequestBody NamespaceReleaseModel model) {
        model.setAppId(appId);
        model.setNamespaceName(namespaceName);
        model.setClusterName(clusterName);

        ReleaseDTO createdRelease = releaseService.publish(model);

        ConfigPublishEvent event = ConfigPublishEvent.instance();
        event.withAppId(appId)
                .withNamespace(namespaceName)
                .withReleaseId(createdRelease.getId())
                .setGrayPublishEvent(true);
        publisher.publishEvent(event);

        return createdRelease;
    }


    @GetMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases/all")
    public List<ReleaseBO> findAllReleases(@PathVariable String appId,
                                           @PathVariable String env,
                                           @PathVariable String clusterName,
                                           @PathVariable String namespaceName,
                                           @Valid @PositiveOrZero(message = "page should be positive or 0") @RequestParam(defaultValue = "0") int page,
                                           @Valid @Positive(message = "size should be positive number") @RequestParam(defaultValue = "5") int size) {
        return releaseService.findAllReleases(appId, namespaceName, clusterName, page, size);
    }

    @GetMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases/active")
    public List<ReleaseDTO> findActiveReleases(@PathVariable String appId,
                                               @PathVariable String env,
                                               @PathVariable String clusterName,
                                               @PathVariable String namespaceName,
                                               @Valid @PositiveOrZero(message = "page should be positive or 0") @RequestParam(defaultValue = "0") int page,
                                               @Valid @Positive(message = "size should be positive number") @RequestParam(defaultValue = "5") int size) {
        return releaseService.findActiveReleases(appId, namespaceName, clusterName, page, size);
    }

    @GetMapping(value = "/envs/{env}/releases/compare")
    public ReleaseCompareResult compareRelease(@PathVariable String env,
                                               @RequestParam long baseReleaseId,
                                               @RequestParam long toCompareReleaseId) {

        return releaseService.compare(baseReleaseId, toCompareReleaseId);

    }


    @PutMapping(path = "/envs/{env}/releases/{releaseId}/rollback")
    public void rollback(@PathVariable String env,
                         @PathVariable long releaseId) {
        Set<Long> releaseIds = new HashSet<>(1);
        releaseIds.add(releaseId);
        List<ReleaseDTO> releases = releaseService.findReleaseByIds(releaseIds);
        if (CollectionUtils.isEmpty(releases)) {
            throw new NotFoundException("release not found");
        }

        ReleaseDTO release = releases.get(0);

        if (release == null) {
            throw new NotFoundException("release not found");
        }

        if (!permissionValidator.hasReleaseNamespacePermission(release.getAppId(), release.getNamespaceName())) {
            throw new AccessDeniedException("Access is denied");
        }

        releaseService.rollback(releaseId);

        ConfigPublishEvent event = ConfigPublishEvent.instance();
        event.withAppId(release.getAppId())
                .withNamespace(release.getNamespaceName())
                .withPreviousReleaseId(releaseId)
                .setRollbackEvent(true);

        publisher.publishEvent(event);
    }
}