package lucky.apollo.portal.controller;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import lucky.apollo.common.constant.GsonType;
import lucky.apollo.common.entity.dto.ReleaseDTO;
import lucky.apollo.common.exception.NotFoundException;
import lucky.apollo.portal.api.AdminServiceApi;
import lucky.apollo.portal.constant.ChangeType;
import lucky.apollo.portal.entity.bo.KeyValueInfo;
import lucky.apollo.portal.entity.bo.ReleaseBO;
import lucky.apollo.portal.entity.model.NamespaceReleaseModel;
import lucky.apollo.portal.entity.vo.ReleaseCompareResult;
import lucky.apollo.portal.listener.ConfigPublishEvent;
import lucky.apollo.portal.resolver.PermissionValidator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.*;

;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@Validated
@RestController
public class ReleaseController {

    private static final Gson gson = new Gson();

    private final AdminServiceApi adminServiceApi;
    private final ApplicationEventPublisher publisher;
    private final PermissionValidator permissionValidator;


    public ReleaseController(AdminServiceApi adminServiceApi, ApplicationEventPublisher publisher, PermissionValidator permissionValidator) {
        this.adminServiceApi = adminServiceApi;
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

        ReleaseDTO createdRelease = adminServiceApi.createRelease(appId, namespaceName,
                model.getReleaseTitle(), model.getReleaseComment(),
                model.getReleasedBy(), model.getIsEmergencyPublish());

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

        ReleaseDTO createdRelease = adminServiceApi.createRelease(appId, namespaceName,
                model.getReleaseTitle(), model.getReleaseComment(),
                model.getReleasedBy(), model.getIsEmergencyPublish());

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
        if (permissionValidator.shouldHideConfigToCurrentUser(appId, namespaceName)) {
            return Collections.emptyList();
        }

        List<ReleaseDTO> releaseDTOs = adminServiceApi.findAllReleases(appId, namespaceName, page, size);

        if (CollectionUtils.isEmpty(releaseDTOs)) {
            return Collections.emptyList();
        }

        List<ReleaseBO> releases = new LinkedList<>();
        for (ReleaseDTO releaseDTO : releaseDTOs) {
            ReleaseBO release = new ReleaseBO();
            release.setBaseInfo(releaseDTO);

            Set<KeyValueInfo> kvEntities = new LinkedHashSet<>();
            Map<String, String> configurations = gson.fromJson(releaseDTO.getConfigurations(), GsonType.CONFIG);
            Set<Map.Entry<String, String>> entries = configurations.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                kvEntities.add(new KeyValueInfo(entry.getKey(), entry.getValue()));
            }
            release.setItems(kvEntities);
            //为了减少数据量
            releaseDTO.setConfigurations("");
            releases.add(release);
        }

        return releases;
    }

    @GetMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases/active")
    public List<ReleaseDTO> findActiveReleases(@PathVariable String appId,
                                               @PathVariable String env,
                                               @PathVariable String clusterName,
                                               @PathVariable String namespaceName,
                                               @Valid @PositiveOrZero(message = "page should be positive or 0") @RequestParam(defaultValue = "0") int page,
                                               @Valid @Positive(message = "size should be positive number") @RequestParam(defaultValue = "5") int size) {

        if (permissionValidator.shouldHideConfigToCurrentUser(appId, namespaceName)) {
            return Collections.emptyList();
        }

        return adminServiceApi.findActiveReleases(appId, namespaceName, page, size);
    }

    @GetMapping(value = "/envs/{env}/releases/compare")
    public ReleaseCompareResult compareRelease(@PathVariable String env,
                                               @RequestParam long baseReleaseId,
                                               @RequestParam long toCompareReleaseId) {

        ReleaseDTO baseRelease = null;
        ReleaseDTO toCompareRelease = null;
        if (baseReleaseId != 0) {
            baseRelease = adminServiceApi.loadRelease(baseReleaseId);
        }

        if (toCompareReleaseId != 0) {
            toCompareRelease = adminServiceApi.loadRelease(toCompareReleaseId);
        }

        Map<String, String> baseReleaseConfiguration = baseRelease == null ? new HashMap<>() :
                gson.fromJson(baseRelease.getConfigurations(), GsonType.CONFIG);
        Map<String, String> toCompareReleaseConfiguration = toCompareRelease == null ? new HashMap<>() :
                gson.fromJson(toCompareRelease.getConfigurations(),
                        GsonType.CONFIG);

        ReleaseCompareResult compareResult = new ReleaseCompareResult();

        //added and modified in firstRelease
        for (Map.Entry<String, String> entry : baseReleaseConfiguration.entrySet()) {
            String key = entry.getKey();
            String firstValue = entry.getValue();
            String secondValue = toCompareReleaseConfiguration.get(key);
            //added
            if (secondValue == null) {
                compareResult.addEntityPair(ChangeType.DELETED, new KeyValueInfo(key, firstValue),
                        new KeyValueInfo(key, null));
            } else if (!Objects.equal(firstValue, secondValue)) {
                compareResult.addEntityPair(ChangeType.MODIFIED, new KeyValueInfo(key, firstValue),
                        new KeyValueInfo(key, secondValue));
            }

        }

        //deleted in firstRelease
        for (Map.Entry<String, String> entry : toCompareReleaseConfiguration.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (baseReleaseConfiguration.get(key) == null) {
                compareResult
                        .addEntityPair(ChangeType.ADDED, new KeyValueInfo(key, ""), new KeyValueInfo(key, value));
            }

        }

        return compareResult;
    }


    @PutMapping(path = "/envs/{env}/releases/{releaseId}/rollback")
    public void rollback(@PathVariable String env,
                         @PathVariable long releaseId) {
        Set<Long> releaseIds = new HashSet<>(1);
        releaseIds.add(releaseId);
        List<ReleaseDTO> releases = adminServiceApi.findReleaseByIds(releaseIds);
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

        adminServiceApi.rollback(releaseId);

        ConfigPublishEvent event = ConfigPublishEvent.instance();
        event.withAppId(release.getAppId())
                .withNamespace(release.getNamespaceName())
                .withPreviousReleaseId(releaseId)
                .setRollbackEvent(true);

        publisher.publishEvent(event);
    }
}