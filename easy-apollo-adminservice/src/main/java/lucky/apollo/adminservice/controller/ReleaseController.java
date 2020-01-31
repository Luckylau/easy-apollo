package lucky.apollo.adminservice.controller;

import com.google.common.base.Splitter;
import lucky.apollo.common.constant.NamespaceBranchStatus;
import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ReleaseDTO;
import lucky.apollo.common.exception.NotFoundException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.entity.ReleasePO;
import lucky.apollo.core.message.MessageSender;
import lucky.apollo.core.message.MessageTopic;
import lucky.apollo.core.service.NamespaceBranchService;
import lucky.apollo.core.service.NamespaceService;
import lucky.apollo.core.service.ReleaseService;
import lucky.apollo.core.utils.ReleaseMessageKeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author luckylau
 * @Date 2019/9/19
 */
@RestController
public class ReleaseController {
    private static final Splitter RELEASES_SPLITTER = Splitter.on(",").omitEmptyStrings()
            .trimResults();

    private final ReleaseService releaseService;

    private final NamespaceService namespaceService;

    private final MessageSender messageSender;

    private final NamespaceBranchService namespaceBranchService;

    public ReleaseController(ReleaseService releaseService, NamespaceService namespaceService, MessageSender messageSender, NamespaceBranchService namespaceBranchService) {
        this.releaseService = releaseService;
        this.namespaceService = namespaceService;
        this.messageSender = messageSender;
        this.namespaceBranchService = namespaceBranchService;
    }

    @GetMapping("/releases/{releaseId}")
    public ReleaseDTO get(@PathVariable("releaseId") long releaseId) {
        ReleasePO release = releaseService.findOne(releaseId);
        if (release == null) {
            throw new NotFoundException(String.format("release not found for %s", releaseId));
        }
        return BeanUtils.transformWithIgnoreNull(ReleaseDTO.class, release);
    }

    @GetMapping("/releases")
    public List<ReleaseDTO> findReleaseByIds(@RequestParam("releaseIds") String releaseIds) {
        Set<Long> releaseIdSet = RELEASES_SPLITTER.splitToList(releaseIds).stream().map(Long::parseLong)
                .collect(Collectors.toSet());

        List<ReleasePO> releases = releaseService.findByReleaseIds(releaseIdSet);

        return BeanUtils.batchTransformWithIgnoreNull(ReleaseDTO.class, releases);
    }

    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/all")
    public List<ReleaseDTO> findAllReleases(@PathVariable("appId") String appId,
                                            @PathVariable("clusterName") String clusterName,
                                            @PathVariable("namespaceName") String namespaceName,
                                            Pageable page) {
        List<ReleasePO> releases = releaseService.findAllReleases(appId, clusterName, namespaceName, page);
        return BeanUtils.batchTransformWithIgnoreNull(ReleaseDTO.class, releases);
    }


    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/active")
    public List<ReleaseDTO> findActiveReleases(@PathVariable("appId") String appId,
                                               @PathVariable("clusterName") String clusterName,
                                               @PathVariable("namespaceName") String namespaceName,
                                               Pageable page) {
        List<ReleasePO> releases = releaseService.findActiveReleases(appId, clusterName, namespaceName, page);
        return BeanUtils.batchTransformWithIgnoreNull(ReleaseDTO.class, releases);
    }

    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/latest")
    public ReleaseDTO getLatest(@PathVariable("appId") String appId,
                                @PathVariable("clusterName") String clusterName,
                                @PathVariable("namespaceName") String namespaceName) {
        ReleasePO release = releaseService.findLatestActiveRelease(appId, clusterName, namespaceName);
        return BeanUtils.transformWithIgnoreNull(ReleaseDTO.class, release);
    }

    @Transactional
    @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases")
    public ReleaseDTO publish(@PathVariable("appId") String appId,
                              @PathVariable("clusterName") String clusterName,
                              @PathVariable("namespaceName") String namespaceName,
                              @RequestParam("name") String releaseName,
                              @RequestParam(name = "comment", required = false) String releaseComment,
                              @RequestParam("operator") String operator,
                              @RequestParam(name = "isEmergencyPublish", defaultValue = "false") boolean isEmergencyPublish) {
        NamespacePO namespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (namespace == null) {
            throw new NotFoundException(String.format("Could not find namespace for %s %s %s", appId,
                    clusterName, namespaceName));
        }
        ReleasePO release = releaseService.publish(namespace, releaseName, releaseComment, operator, isEmergencyPublish);

        messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName),
                MessageTopic.APOLLO_RELEASE_TOPIC);
        return BeanUtils.transformWithIgnoreNull(ReleaseDTO.class, release);
    }


    /**
     * merge branch items to master and publish master
     *
     * @return published result
     */
    @Transactional
    @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/updateAndPublish")
    public ReleaseDTO updateAndPublish(@PathVariable("appId") String appId,
                                       @PathVariable("clusterName") String clusterName,
                                       @PathVariable("namespaceName") String namespaceName,
                                       @RequestParam("releaseName") String releaseName,
                                       @RequestParam("branchName") String branchName,
                                       @RequestParam(value = "deleteBranch", defaultValue = "true") boolean deleteBranch,
                                       @RequestParam(name = "releaseComment", required = false) String releaseComment,
                                       @RequestParam(name = "isEmergencyPublish", defaultValue = "false") boolean isEmergencyPublish,
                                       @RequestBody ItemChangeSetsDTO changeSets) {
        NamespacePO namespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (namespace == null) {
            throw new NotFoundException(String.format("Could not find namespace for %s %s %s", appId,
                    clusterName, namespaceName));
        }

        ReleasePO release = releaseService.mergeBranchChangeSetsAndRelease(namespace, branchName, releaseName,
                releaseComment, isEmergencyPublish, changeSets);

        if (deleteBranch) {
            namespaceBranchService.deleteBranch(appId, clusterName, namespaceName, branchName,
                    NamespaceBranchStatus.MERGED, changeSets.getDataChangeLastModifiedBy());
        }

        messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName),
                MessageTopic.APOLLO_RELEASE_TOPIC);

        return BeanUtils.transformWithIgnoreNull(ReleaseDTO.class, release);

    }

    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/releases/{releaseId}/rollback")
    public void rollback(@PathVariable("releaseId") long releaseId,
                         @RequestParam("operator") String operator) {

        ReleasePO release = releaseService.rollback(releaseId, operator);

        String appId = release.getAppId();
        String namespaceName = release.getNamespaceName();
        String clusterName = release.getClusterName();
        //send release message
        messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName),
                MessageTopic.APOLLO_RELEASE_TOPIC);
    }

    @Transactional
    @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/gray-del-releases")
    public ReleaseDTO publish(@PathVariable("appId") String appId,
                              @PathVariable("clusterName") String clusterName,
                              @PathVariable("namespaceName") String namespaceName,
                              @RequestParam("operator") String operator,
                              @RequestParam("releaseName") String releaseName,
                              @RequestParam(name = "comment", required = false) String releaseComment,
                              @RequestParam(name = "isEmergencyPublish", defaultValue = "false") boolean isEmergencyPublish,
                              @RequestParam(name = "grayDelKeys") Set<String> grayDelKeys) {
        NamespacePO namespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (namespace == null) {
            throw new NotFoundException(String.format("Could not find namespace for %s %s %s", appId,
                    clusterName, namespaceName));
        }

        ReleasePO release = releaseService.grayDeletionPublish(namespace, releaseName, releaseComment, operator, isEmergencyPublish, grayDelKeys);

        messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName),
                MessageTopic.APOLLO_RELEASE_TOPIC);
        return BeanUtils.transformWithIgnoreNull(ReleaseDTO.class, release);
    }


}
