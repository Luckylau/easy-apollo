package lucky.apollo.core.service;

import lucky.apollo.core.entity.GrayReleaseRulePO;
import lucky.apollo.core.entity.NamespacePO;

/**
 * @Author luckylau
 * @Date 2019/9/19
 */
public interface NamespaceBranchService {

    NamespacePO createBranch(String appId, String parentClusterName, String namespaceName, String operator);

    NamespacePO findBranch(String appId, String parentClusterName, String namespaceName);

    void updateBranchGrayRules(String appId, String clusterName, String namespaceName,
                               String branchName, GrayReleaseRulePO newRules);


    GrayReleaseRulePO updateRulesReleaseId(String appId, String clusterName,
                                           String namespaceName, String branchName,
                                           long latestReleaseId, String operator);

    void deleteBranch(String appId, String clusterName, String namespaceName,
                      String branchName, int branchStatus, String operator);

    GrayReleaseRulePO findBranchGrayRules(String appId, String clusterName, String namespaceName,
                                          String branchName);

}
