package lucky.apollo.core.service.impl;

import com.google.common.collect.Maps;
import lucky.apollo.common.constant.NamespaceBranchStatus;
import lucky.apollo.common.constant.ReleaseOperation;
import lucky.apollo.common.constant.ReleaseOperationContext;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.UniqueKeyGenerator;
import lucky.apollo.core.constant.OpAudit;
import lucky.apollo.core.entity.ClusterPO;
import lucky.apollo.core.entity.GrayReleaseRulePO;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.entity.ReleasePO;
import lucky.apollo.core.repository.GrayReleaseRuleRepository;
import lucky.apollo.core.service.*;
import lucky.apollo.core.utils.GrayReleaseRuleItemTransformer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/9/19
 */
@Service
public class NamespaceBranchServiceImpl implements NamespaceBranchService {

    private final AuditService auditService;
    private final GrayReleaseRuleRepository grayReleaseRuleRepository;
    private final ReleaseService releaseService;
    private final NamespaceService namespaceService;
    private final ReleaseHistoryService releaseHistoryService;
    private final ClusterService clusterService;


    public NamespaceBranchServiceImpl(
            final AuditService auditService,
            final GrayReleaseRuleRepository grayReleaseRuleRepository,
            final ReleaseService releaseService,
            final NamespaceService namespaceService,
            final ReleaseHistoryService releaseHistoryService,
            final ClusterService clusterService) {
        this.auditService = auditService;
        this.grayReleaseRuleRepository = grayReleaseRuleRepository;
        this.releaseService = releaseService;
        this.namespaceService = namespaceService;
        this.releaseHistoryService = releaseHistoryService;
        this.clusterService = clusterService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NamespacePO createBranch(String appId, String parentClusterName, String namespaceName, String operator) {
        NamespacePO childNamespace = findBranch(appId, parentClusterName, namespaceName);
        if (childNamespace != null) {
            throw new BadRequestException("namespace already has branch");
        }

        ClusterPO parentCluster = clusterService.findOne(appId, parentClusterName);
        if (parentCluster == null || parentCluster.getParentClusterId() != 0) {
            throw new BadRequestException("cluster not exist or illegal cluster");
        }

        //create child cluster
        ClusterPO childCluster = createChildCluster(appId, parentCluster, namespaceName, operator);

        ClusterPO createdChildCluster = clusterService.saveWithoutInstanceOfAppNamespaces(childCluster);

        //create child namespace
        childNamespace = createNamespaceBranch(appId, createdChildCluster.getName(),
                namespaceName, operator);
        return namespaceService.save(childNamespace);
    }


    private ClusterPO createChildCluster(String appId, ClusterPO parentCluster,
                                         String namespaceName, String operator) {

        ClusterPO childCluster = new ClusterPO();
        childCluster.setAppId(appId);
        childCluster.setParentClusterId(parentCluster.getId());
        childCluster.setName(UniqueKeyGenerator.generate(appId, parentCluster.getName(), namespaceName));
        childCluster.setDataChangeCreatedBy(operator);
        childCluster.setDataChangeLastModifiedBy(operator);

        return childCluster;
    }

    private NamespacePO createNamespaceBranch(String appId, String clusterName, String namespaceName, String operator) {
        NamespacePO childNamespace = new NamespacePO();
        childNamespace.setAppId(appId);
        childNamespace.setClusterName(clusterName);
        childNamespace.setNamespaceName(namespaceName);
        childNamespace.setDataChangeLastModifiedBy(operator);
        childNamespace.setDataChangeCreatedBy(operator);
        return childNamespace;
    }


    @Override
    public NamespacePO findBranch(String appId, String parentClusterName, String namespaceName) {
        return namespaceService.findChildNamespace(appId, parentClusterName, namespaceName);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateBranchGrayRules(String appId, String clusterName, String namespaceName, String branchName, GrayReleaseRulePO newRules) {
        doUpdateBranchGrayRules(appId, clusterName, namespaceName, branchName, newRules, true, ReleaseOperation.APPLY_GRAY_RULES);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public GrayReleaseRulePO updateRulesReleaseId(String appId, String clusterName, String namespaceName, String branchName, long latestReleaseId, String operator) {
        GrayReleaseRulePO oldRules = grayReleaseRuleRepository.
                findTopByAppIdAndClusterNameAndNamespaceNameAndBranchNameOrderByIdDesc(appId, clusterName, namespaceName, branchName);

        if (oldRules == null) {
            return null;
        }

        GrayReleaseRulePO newRules = new GrayReleaseRulePO();
        newRules.setBranchStatus(NamespaceBranchStatus.ACTIVE);
        newRules.setReleaseId(latestReleaseId);
        newRules.setRules(oldRules.getRules());
        newRules.setAppId(oldRules.getAppId());
        newRules.setClusterName(oldRules.getClusterName());
        newRules.setNamespaceName(oldRules.getNamespaceName());
        newRules.setBranchName(oldRules.getBranchName());
        newRules.setDataChangeCreatedBy(operator);
        newRules.setDataChangeLastModifiedBy(operator);

        grayReleaseRuleRepository.save(newRules);

        grayReleaseRuleRepository.delete(oldRules);

        return newRules;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBranch(String appId, String clusterName, String namespaceName, String branchName, int branchStatus, String operator) {
        ClusterPO toDeleteCluster = clusterService.findOne(appId, branchName);
        if (toDeleteCluster == null) {
            return;
        }

        ReleasePO latestBranchRelease = releaseService.findLatestActiveRelease(appId, branchName, namespaceName);

        long latestBranchReleaseId = latestBranchRelease != null ? latestBranchRelease.getId() : 0;

        //update branch rules
        GrayReleaseRulePO deleteRule = new GrayReleaseRulePO();
        deleteRule.setRules("[]");
        deleteRule.setAppId(appId);
        deleteRule.setClusterName(clusterName);
        deleteRule.setNamespaceName(namespaceName);
        deleteRule.setBranchName(branchName);
        deleteRule.setBranchStatus(branchStatus);
        deleteRule.setDataChangeLastModifiedBy(operator);
        deleteRule.setDataChangeCreatedBy(operator);

        doUpdateBranchGrayRules(appId, clusterName, namespaceName, branchName, deleteRule, false, -1);

        //delete branch cluster
        clusterService.delete(toDeleteCluster.getId(), operator);

        int releaseOperation = branchStatus == NamespaceBranchStatus.MERGED ? ReleaseOperation
                .GRAY_RELEASE_DELETED_AFTER_MERGE : ReleaseOperation.ABANDON_GRAY_RELEASE;

        releaseHistoryService.createReleaseHistory(appId, clusterName, namespaceName, branchName, latestBranchReleaseId,
                latestBranchReleaseId, releaseOperation, null, operator);

        auditService.audit("Branch", toDeleteCluster.getId(), OpAudit.DELETE, operator);
    }

    @Override
    public GrayReleaseRulePO findBranchGrayRules(String appId, String clusterName, String namespaceName, String branchName) {
        return grayReleaseRuleRepository
                .findTopByAppIdAndClusterNameAndNamespaceNameAndBranchNameOrderByIdDesc(appId, clusterName, namespaceName, branchName);
    }

    private void doUpdateBranchGrayRules(String appId, String clusterName, String namespaceName,
                                         String branchName, GrayReleaseRulePO newRules, boolean recordReleaseHistory, int releaseOperation) {
        GrayReleaseRulePO oldRules = grayReleaseRuleRepository
                .findTopByAppIdAndClusterNameAndNamespaceNameAndBranchNameOrderByIdDesc(appId, clusterName, namespaceName, branchName);

        ReleasePO latestBranchRelease = releaseService.findLatestActiveRelease(appId, branchName, namespaceName);

        long latestBranchReleaseId = latestBranchRelease != null ? latestBranchRelease.getId() : 0;

        newRules.setReleaseId(latestBranchReleaseId);

        grayReleaseRuleRepository.save(newRules);

        //delete old rules
        if (oldRules != null) {
            grayReleaseRuleRepository.delete(oldRules);
        }

        if (recordReleaseHistory) {
            Map<String, Object> releaseOperationContext = Maps.newHashMap();
            releaseOperationContext.put(ReleaseOperationContext.RULES, GrayReleaseRuleItemTransformer
                    .batchTransformFromJSON(newRules.getRules()));
            if (oldRules != null) {
                releaseOperationContext.put(ReleaseOperationContext.OLD_RULES,
                        GrayReleaseRuleItemTransformer.batchTransformFromJSON(oldRules.getRules()));
            }
            releaseHistoryService.createReleaseHistory(appId, clusterName, namespaceName, branchName, latestBranchReleaseId,
                    latestBranchReleaseId, releaseOperation, releaseOperationContext, newRules.getDataChangeLastModifiedBy());
        }
    }
}
