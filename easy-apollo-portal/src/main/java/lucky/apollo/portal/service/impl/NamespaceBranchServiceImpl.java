package lucky.apollo.portal.service.impl;

import lucky.apollo.common.entity.dto.*;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.portal.adminsevice.api.AdminServiceApi;
import lucky.apollo.portal.component.txtresolver.ItemsComparator;
import lucky.apollo.portal.entity.bo.NamespaceInfo;
import lucky.apollo.portal.service.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/10/10
 */
@Service
public class NamespaceBranchServiceImpl implements NamespaceBranchService {

    private final ItemsComparator itemsComparator;
    private final UserService userService;
    private final NamespaceService namespaceService;
    private final ItemService itemService;
    private final AdminServiceApi adminServiceApi;
    private final ReleaseService releaseService;

    public NamespaceBranchServiceImpl(
            final ItemsComparator itemsComparator,
            final UserService userService,
            final NamespaceService namespaceService,
            final ItemService itemService,
            final AdminServiceApi adminServiceApi,
            final ReleaseService releaseService) {
        this.itemsComparator = itemsComparator;
        this.userService = userService;
        this.namespaceService = namespaceService;
        this.itemService = itemService;
        this.adminServiceApi = adminServiceApi;
        this.releaseService = releaseService;
    }

    @Override
    public NamespaceDTO createBranch(String appId, String parentClusterName, String namespaceName) {
        String operator = userService.getCurrentUser().getUserId();
        return createBranch(appId, parentClusterName, namespaceName, operator);
    }

    @Override
    public NamespaceDTO createBranch(String appId, String parentClusterName, String namespaceName, String operator) {
        return adminServiceApi.createBranch(appId, namespaceName, parentClusterName,
                operator);
    }

    @Override
    public GrayReleaseRuleDTO findBranchGrayRules(String appId, String clusterName, String namespaceName, String branchName) {
        return adminServiceApi.findBranchGrayRules(appId, clusterName, namespaceName, branchName);
    }

    @Override
    public void updateBranchGrayRules(String appId, String clusterName, String namespaceName, String branchName, GrayReleaseRuleDTO rules) {
        String operator = userService.getCurrentUser().getUserId();
        updateBranchGrayRules(appId, clusterName, namespaceName, branchName, rules, operator);
    }

    @Override
    public void updateBranchGrayRules(String appId, String clusterName, String namespaceName, String branchName, GrayReleaseRuleDTO rules, String operator) {
        rules.setDataChangeCreatedBy(operator);
        rules.setDataChangeLastModifiedBy(operator);
        adminServiceApi.updateBranchGrayRules(appId, namespaceName, clusterName, branchName, rules);
    }

    @Override
    public void deleteBranch(String appId, String clusterName, String namespaceName, String branchName) {
        String operator = userService.getCurrentUser().getUserId();
        deleteBranch(appId, clusterName, namespaceName, branchName, operator);
    }

    @Override
    public void deleteBranch(String appId, String clusterName, String namespaceName, String branchName, String operator) {
        adminServiceApi.deleteBranch(appId, namespaceName, clusterName, branchName, operator);
    }

    @Override
    public ReleaseDTO merge(String appId, String clusterName, String namespaceName, String branchName, String title, String comment, boolean isEmergencyPublish, boolean deleteBranch) {
        String operator = userService.getCurrentUser().getUserId();
        return merge(appId, clusterName, namespaceName, branchName, title, comment, isEmergencyPublish, deleteBranch, operator);
    }

    @Override
    public ReleaseDTO merge(String appId, String clusterName, String namespaceName, String branchName, String title, String comment, boolean isEmergencyPublish, boolean deleteBranch, String operator) {
        ItemChangeSetsDTO changeSets = calculateBranchChangeSet(appId, clusterName, namespaceName, branchName, operator);
        return releaseService.updateAndPublish(appId, namespaceName, clusterName
                , title, comment,
                branchName, isEmergencyPublish, deleteBranch, changeSets);
    }

    @Override
    public ItemChangeSetsDTO calculateBranchChangeSet(String appId, String clusterName, String namespaceName, String branchName, String operator) {
        NamespaceInfo parentNamespace = namespaceService.loadNamespaceBO(appId, clusterName, namespaceName);

        if (parentNamespace == null) {
            throw new BadRequestException("base namespace not existed");
        }

        if (parentNamespace.getItemModifiedCnt() > 0) {
            throw new BadRequestException("Merge operation failed. Because master has modified items");
        }

        List<ItemDTO> masterItems = itemService.findItems(appId, namespaceName, clusterName);

        List<ItemDTO> branchItems = itemService.findItems(appId, branchName, namespaceName);

        ItemChangeSetsDTO changeSets = itemsComparator.compareIgnoreBlankAndCommentItem(parentNamespace.getBaseInfo().getId(),
                masterItems, branchItems);
        changeSets.setDeleteItems(Collections.emptyList());
        changeSets.setDataChangeLastModifiedBy(operator);
        return changeSets;
    }

    @Override
    public NamespaceDTO findBranchBaseInfo(String appId, String clusterName, String namespaceName) {
        return adminServiceApi.findBranch(appId, namespaceName, clusterName);
    }

    @Override
    public NamespaceInfo findBranch(String appId, String clusterName, String namespaceName) {
        NamespaceDTO namespaceDTO = findBranchBaseInfo(appId, clusterName, namespaceName);
        if (namespaceDTO == null) {
            return null;
        }
        return namespaceService.loadNamespaceBO(appId, namespaceDTO.getClusterName(), namespaceName);
    }
}
