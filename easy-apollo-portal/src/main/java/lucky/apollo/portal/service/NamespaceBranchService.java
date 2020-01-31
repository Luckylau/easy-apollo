package lucky.apollo.portal.service;

import lucky.apollo.common.entity.dto.GrayReleaseRuleDTO;
import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.NamespaceDTO;
import lucky.apollo.common.entity.dto.ReleaseDTO;
import lucky.apollo.portal.entity.bo.NamespaceInfo;

/**
 * @Author luckylau
 * @Date 2019/10/10
 */
public interface NamespaceBranchService {

    NamespaceDTO createBranch(String appId, String clusterName, String namespaceName);

    NamespaceDTO createBranch(String appId, String clusterName, String namespaceName, String operator);

    GrayReleaseRuleDTO findBranchGrayRules(String appId, String clusterName, String namespaceName, String branchName);

    void updateBranchGrayRules(String appId, String clusterName, String namespaceName,
                               String branchName, GrayReleaseRuleDTO rules);

    void updateBranchGrayRules(String appId, String clusterName, String namespaceName,
                               String branchName, GrayReleaseRuleDTO rules, String operator);

    void deleteBranch(String appId, String clusterName, String namespaceName,
                      String branchName);

    void deleteBranch(String appId, String clusterName, String namespaceName, String branchName, String operator);

    ReleaseDTO merge(String appId, String clusterName, String namespaceName,
                     String branchName, String title, String comment,
                     boolean isEmergencyPublish, boolean deleteBranch);

    ReleaseDTO merge(String appId, String clusterName, String namespaceName,
                     String branchName, String title, String comment,
                     boolean isEmergencyPublish, boolean deleteBranch, String operator);

    ItemChangeSetsDTO calculateBranchChangeSet(String appId, String clusterName, String namespaceName,
                                               String branchName, String operator);

    NamespaceDTO findBranchBaseInfo(String appId, String clusterName, String namespaceName);

    NamespaceInfo findBranch(String appId, String clusterName, String namespaceName);
}
