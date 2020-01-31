package lucky.apollo.adminservice.controller;

import lucky.apollo.common.constant.NamespaceBranchStatus;
import lucky.apollo.common.entity.dto.GrayReleaseRuleDTO;
import lucky.apollo.common.entity.dto.NamespaceDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.entity.GrayReleaseRulePO;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.message.MessageSender;
import lucky.apollo.core.message.MessageTopic;
import lucky.apollo.core.service.NamespaceBranchService;
import lucky.apollo.core.service.NamespaceService;
import lucky.apollo.core.utils.GrayReleaseRuleItemTransformer;
import lucky.apollo.core.utils.ReleaseMessageKeyGenerator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @Author luckylau
 * @Date 2019/10/10
 */
@RestController
public class NamespaceBranchController {
    private final MessageSender messageSender;
    private final NamespaceBranchService namespaceBranchService;
    private final NamespaceService namespaceService;

    public NamespaceBranchController(
            final MessageSender messageSender,
            final NamespaceBranchService namespaceBranchService,
            final NamespaceService namespaceService) {
        this.messageSender = messageSender;
        this.namespaceBranchService = namespaceBranchService;
        this.namespaceService = namespaceService;
    }


    @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches")
    public NamespaceDTO createBranch(@PathVariable String appId,
                                     @PathVariable String clusterName,
                                     @PathVariable String namespaceName,
                                     @RequestParam("operator") String operator) {

        checkNamespace(appId, clusterName, namespaceName);

        NamespacePO createdBranch = namespaceBranchService.createBranch(appId, clusterName, namespaceName, operator);

        return BeanUtils.transformWithIgnoreNull(NamespaceDTO.class, createdBranch);
    }

    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules")
    public GrayReleaseRuleDTO findBranchGrayRules(@PathVariable String appId,
                                                  @PathVariable String clusterName,
                                                  @PathVariable String namespaceName,
                                                  @PathVariable String branchName) {

        checkBranch(appId, clusterName, namespaceName, branchName);

        GrayReleaseRulePO rules = namespaceBranchService.findBranchGrayRules(appId, clusterName, namespaceName, branchName);
        if (rules == null) {
            return null;
        }
        GrayReleaseRuleDTO ruleDTO =
                new GrayReleaseRuleDTO(rules.getAppId(), rules.getClusterName(), rules.getNamespaceName(),
                        rules.getBranchName());

        ruleDTO.setReleaseId(rules.getReleaseId());

        ruleDTO.setRuleItems(GrayReleaseRuleItemTransformer.batchTransformFromJSON(rules.getRules()));

        return ruleDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules")
    public void updateBranchGrayRules(@PathVariable String appId, @PathVariable String clusterName,
                                      @PathVariable String namespaceName, @PathVariable String branchName,
                                      @RequestBody GrayReleaseRuleDTO newRuleDto) {

        checkBranch(appId, clusterName, namespaceName, branchName);

        GrayReleaseRulePO newRules = BeanUtils.transformWithIgnoreNull(GrayReleaseRulePO.class, newRuleDto);
        newRules.setRules(GrayReleaseRuleItemTransformer.batchTransformToJSON(newRuleDto.getRuleItems()));
        newRules.setBranchStatus(NamespaceBranchStatus.ACTIVE);

        namespaceBranchService.updateBranchGrayRules(appId, clusterName, namespaceName, branchName, newRules);

        messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName),
                MessageTopic.APOLLO_RELEASE_TOPIC);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}")
    public void deleteBranch(@PathVariable String appId, @PathVariable String clusterName,
                             @PathVariable String namespaceName, @PathVariable String branchName,
                             @RequestParam("operator") String operator) {

        checkBranch(appId, clusterName, namespaceName, branchName);

        namespaceBranchService
                .deleteBranch(appId, clusterName, namespaceName, branchName, NamespaceBranchStatus.DELETED, operator);

        messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName),
                MessageTopic.APOLLO_RELEASE_TOPIC);

    }

    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches")
    public NamespaceDTO loadNamespaceBranch(@PathVariable String appId, @PathVariable String clusterName,
                                            @PathVariable String namespaceName) {

        checkNamespace(appId, clusterName, namespaceName);

        NamespacePO childNamespace = namespaceBranchService.findBranch(appId, clusterName, namespaceName);
        if (childNamespace == null) {
            return null;
        }

        return BeanUtils.transformWithIgnoreNull(NamespaceDTO.class, childNamespace);
    }

    private void checkBranch(String appId, String clusterName, String namespaceName, String branchName) {
        //1. check parent namespace
        checkNamespace(appId, clusterName, namespaceName);

        //2. check child namespace
        NamespacePO childNamespace = namespaceService.findOne(appId, branchName, namespaceName);
        if (childNamespace == null) {
            throw new BadRequestException(String.format("Namespace's branch not exist. AppId = %s, ClusterName = %s, "
                            + "NamespaceName = %s, BranchName = %s",
                    appId, clusterName, namespaceName, branchName));
        }

    }

    private void checkNamespace(String appId, String clusterName, String namespaceName) {
        NamespacePO parentNamespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (parentNamespace == null) {
            throw new BadRequestException(String.format("Namespace not exist. AppId = %s, ClusterName = %s, NamespaceName = %s", appId,
                    clusterName, namespaceName));
        }
    }

}
