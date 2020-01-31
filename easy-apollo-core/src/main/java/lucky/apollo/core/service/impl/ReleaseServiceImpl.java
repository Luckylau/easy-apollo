package lucky.apollo.core.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lucky.apollo.common.constant.GsonType;
import lucky.apollo.common.constant.ReleaseOperation;
import lucky.apollo.common.constant.ReleaseOperationContext;
import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.exception.NotFoundException;
import lucky.apollo.common.utils.StringUtils;
import lucky.apollo.core.constant.OpAudit;
import lucky.apollo.core.entity.*;
import lucky.apollo.core.repository.ReleaseRepository;
import lucky.apollo.core.service.*;
import lucky.apollo.core.utils.GrayReleaseRuleItemTransformer;
import lucky.apollo.core.utils.ReleaseKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Service
public class ReleaseServiceImpl implements ReleaseService {

    private static final Gson gson = new Gson();

    private static final Set<Integer> BRANCH_RELEASE_OPERATIONS = Sets
            .newHashSet(ReleaseOperation.GRAY_RELEASE, ReleaseOperation.MASTER_NORMAL_RELEASE_MERGE_TO_GRAY,
                    ReleaseOperation.MATER_ROLLBACK_MERGE_TO_GRAY);
    private static final Pageable FIRST_ITEM = PageRequest.of(0, 1);

    private static final Type OPERATION_CONTEXT_TYPE_REFERENCE = new TypeToken<Map<String, Object>>() {
    }.getType();

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private ItemSetService itemSetService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private NamespaceLockService namespaceLockService;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private ReleaseHistoryService releaseHistoryService;

    @Autowired
    private NamespaceBranchService namespaceBranchService;

    @Override
    public ReleasePO findOne(long releaseId) {
        return releaseRepository.findById(releaseId).orElse(null);
    }


    @Override
    public ReleasePO findActiveOne(long releaseId) {
        return releaseRepository.findByIdAndIsAbandonedFalse(releaseId);
    }

    @Override
    public List<ReleasePO> findByReleaseIds(Set<Long> releaseIds) {
        Iterable<ReleasePO> releases = releaseRepository.findAllById(releaseIds);
        return Lists.newArrayList(releases);
    }

    @Override
    public List<ReleasePO> findByReleaseKeys(Set<String> releaseKeys) {
        return releaseRepository.findByReleaseKeyIn(releaseKeys);
    }

    @Override
    public ReleasePO findLatestActiveRelease(NamespacePO namespace) {
        return findLatestActiveRelease(namespace.getAppId(),
                namespace.getClusterName(), namespace.getNamespaceName());

    }

    @Override
    public ReleasePO findLatestActiveRelease(String appId, String clusterName, String namespaceName) {
        return releaseRepository.findFirstByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(appId,
                clusterName,
                namespaceName);
    }

    @Override
    public List<ReleasePO> findAllReleases(String appId, String clusterName, String namespaceName, Pageable page) {
        List<ReleasePO> releases = releaseRepository.findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(appId,
                clusterName,
                namespaceName,
                page);
        if (releases == null) {
            return Collections.emptyList();
        }
        return releases;
    }

    @Override
    public List<ReleasePO> findActiveReleases(String appId, String clusterName, String namespaceName, Pageable page) {
        List<ReleasePO>
                releases =
                releaseRepository.findByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(appId, clusterName,
                        namespaceName,
                        page);
        if (releases == null) {
            return Collections.emptyList();
        }
        return releases;
    }

    @Transactional
    @Override
    public ReleasePO mergeBranchChangeSetsAndRelease(NamespacePO namespace, String branchName, String releaseName,
                                                     String releaseComment, boolean isEmergencyPublish,
                                                     ItemChangeSetsDTO changeSets) {

        checkLock(namespace, isEmergencyPublish, changeSets.getDataChangeLastModifiedBy());

        itemSetService.updateSet(namespace, changeSets);

        ReleasePO branchRelease = findLatestActiveRelease(namespace.getAppId(), branchName, namespace
                .getNamespaceName());
        long branchReleaseId = branchRelease == null ? 0 : branchRelease.getId();

        Map<String, String> operateNamespaceItems = getNamespaceItems(namespace);

        Map<String, Object> operationContext = Maps.newHashMap();
        operationContext.put(ReleaseOperationContext.SOURCE_BRANCH, branchName);
        operationContext.put(ReleaseOperationContext.BASE_RELEASE_ID, branchReleaseId);
        operationContext.put(ReleaseOperationContext.IS_EMERGENCY_PUBLISH, isEmergencyPublish);

        return masterRelease(namespace, releaseName, releaseComment, operateNamespaceItems,
                changeSets.getDataChangeLastModifiedBy(),
                ReleaseOperation.GRAY_RELEASE_MERGE_TO_MASTER, operationContext);

    }

    @Transactional
    @Override
    public ReleasePO publish(NamespacePO namespace, String releaseName, String releaseComment,
                             String operator, boolean isEmergencyPublish) {

        checkLock(namespace, isEmergencyPublish, operator);

        Map<String, String> operateNamespaceItems = getNamespaceItems(namespace);

        //master release
        Map<String, Object> operationContext = Maps.newHashMap();
        operationContext.put(ReleaseOperationContext.IS_EMERGENCY_PUBLISH, isEmergencyPublish);

        ReleasePO release = masterRelease(namespace, releaseName, releaseComment, operateNamespaceItems,
                operator, ReleaseOperation.NORMAL_RELEASE, operationContext);

        return release;
    }

    private ReleasePO publishBranchNamespace(NamespacePO parentNamespace, NamespacePO childNamespace,
                                             Map<String, String> childNamespaceItems,
                                             String releaseName, String releaseComment,
                                             String operator, boolean isEmergencyPublish, Set<String> grayDelKeys) {
        ReleasePO parentLatestRelease = findLatestActiveRelease(parentNamespace);
        Map<String, String> parentConfigurations = parentLatestRelease != null ?
                gson.fromJson(parentLatestRelease.getConfigurations(),
                        GsonType.CONFIG) : new HashMap<>();
        long baseReleaseId = parentLatestRelease == null ? 0 : parentLatestRelease.getId();

        Map<String, String> configsToPublish = mergeConfiguration(parentConfigurations, childNamespaceItems);

        if (!(grayDelKeys == null || grayDelKeys.size() == 0)) {
            for (String key : grayDelKeys) {
                configsToPublish.remove(key);
            }
        }

        return branchRelease(parentNamespace, childNamespace, releaseName, releaseComment,
                configsToPublish, baseReleaseId, operator, ReleaseOperation.GRAY_RELEASE, isEmergencyPublish,
                childNamespaceItems.keySet());

    }

    @Transactional
    @Override
    public ReleasePO grayDeletionPublish(NamespacePO namespace, String releaseName, String releaseComment,
                                         String operator, boolean isEmergencyPublish, Set<String> grayDelKeys) {

        Map<String, String> operateNamespaceItems = getNamespaceItems(namespace);

        NamespacePO parentNamespace = namespaceService.findParentNamespace(namespace);

        //branch release
        if (parentNamespace != null) {
            return publishBranchNamespace(parentNamespace, namespace, operateNamespaceItems,
                    releaseName, releaseComment, operator, isEmergencyPublish, grayDelKeys);
        } else {
            throw new NotFoundException("Parent namespace not found");
        }
    }

    private void checkLock(NamespacePO namespace, boolean isEmergencyPublish, String operator) {
        /*if (!isEmergencyPublish) {
            NamespaceLockPO lock = namespaceLockService.findLock(namespace.getId());
            if (lock != null && lock.getDataChangeCreatedBy().equals(operator)) {
                throw new BadRequestException("Config can not be published by yourself.");
            }
        }*/
    }

    private void mergeFromMasterAndPublishBranch(NamespacePO parentNamespace, NamespacePO childNamespace,
                                                 Map<String, String> parentNamespaceItems,
                                                 String releaseName, String releaseComment,
                                                 String operator, ReleasePO masterPreviousRelease,
                                                 ReleasePO parentRelease, boolean isEmergencyPublish) {
        //create release for child namespace
        ReleasePO childNamespaceLatestActiveRelease = findLatestActiveRelease(childNamespace);

        Map<String, String> childReleaseConfiguration;
        Collection<String> branchReleaseKeys;
        if (childNamespaceLatestActiveRelease != null) {
            childReleaseConfiguration = gson.fromJson(childNamespaceLatestActiveRelease.getConfigurations(), GsonType.CONFIG);
            branchReleaseKeys = getBranchReleaseKeys(childNamespaceLatestActiveRelease.getId());
        } else {
            childReleaseConfiguration = Collections.emptyMap();
            branchReleaseKeys = null;
        }

        Map<String, String> parentNamespaceOldConfiguration = masterPreviousRelease == null ?
                null : gson.fromJson(masterPreviousRelease.getConfigurations(),
                GsonType.CONFIG);

        Map<String, String> childNamespaceToPublishConfigs =
                calculateChildNamespaceToPublishConfiguration(parentNamespaceOldConfiguration, parentNamespaceItems,
                        childReleaseConfiguration, branchReleaseKeys);

        //compare
        if (!childNamespaceToPublishConfigs.equals(childReleaseConfiguration)) {
            branchRelease(parentNamespace, childNamespace, releaseName, releaseComment,
                    childNamespaceToPublishConfigs, parentRelease.getId(), operator,
                    ReleaseOperation.MASTER_NORMAL_RELEASE_MERGE_TO_GRAY, isEmergencyPublish, branchReleaseKeys);
        }

    }

    private Collection<String> getBranchReleaseKeys(long releaseId) {
        Page<ReleaseHistoryPO> releaseHistories = releaseHistoryService
                .findByReleaseIdAndOperationInOrderByIdDesc(releaseId, BRANCH_RELEASE_OPERATIONS, FIRST_ITEM);

        if (!releaseHistories.hasContent()) {
            return null;
        }

        Map<String, Object> operationContext = gson
                .fromJson(releaseHistories.getContent().get(0).getOperationContext(), OPERATION_CONTEXT_TYPE_REFERENCE);

        if (operationContext == null || !operationContext.containsKey(ReleaseOperationContext.BRANCH_RELEASE_KEYS)) {
            return null;
        }

        return (Collection<String>) operationContext.get(ReleaseOperationContext.BRANCH_RELEASE_KEYS);
    }

    private ReleasePO publishBranchNamespace(NamespacePO parentNamespace, NamespacePO childNamespace,
                                             Map<String, String> childNamespaceItems,
                                             String releaseName, String releaseComment,
                                             String operator, boolean isEmergencyPublish) {
        return publishBranchNamespace(parentNamespace, childNamespace, childNamespaceItems, releaseName, releaseComment,
                operator, isEmergencyPublish, null);

    }

    private ReleasePO masterRelease(NamespacePO namespace, String releaseName, String releaseComment,
                                    Map<String, String> configurations, String operator,
                                    int releaseOperation, Map<String, Object> operationContext) {
        ReleasePO lastActiveRelease = findLatestActiveRelease(namespace);
        long previousReleaseId = lastActiveRelease == null ? 0 : lastActiveRelease.getId();
        ReleasePO release = createRelease(namespace, releaseName, releaseComment,
                configurations, operator);

        releaseHistoryService.createReleaseHistory(namespace.getAppId(), namespace.getClusterName(),
                namespace.getNamespaceName(), namespace.getClusterName(),
                release.getId(), previousReleaseId, releaseOperation,
                operationContext, operator);

        return release;
    }

    private ReleasePO branchRelease(NamespacePO parentNamespace, NamespacePO childNamespace,
                                    String releaseName, String releaseComment,
                                    Map<String, String> configurations, long baseReleaseId,
                                    String operator, int releaseOperation, boolean isEmergencyPublish, Collection<String> branchReleaseKeys) {
        ReleasePO previousRelease = findLatestActiveRelease(childNamespace.getAppId(),
                childNamespace.getClusterName(),
                childNamespace.getNamespaceName());
        long previousReleaseId = previousRelease == null ? 0 : previousRelease.getId();

        Map<String, Object> releaseOperationContext = Maps.newHashMap();
        releaseOperationContext.put(ReleaseOperationContext.BASE_RELEASE_ID, baseReleaseId);
        releaseOperationContext.put(ReleaseOperationContext.IS_EMERGENCY_PUBLISH, isEmergencyPublish);
        releaseOperationContext.put(ReleaseOperationContext.BRANCH_RELEASE_KEYS, branchReleaseKeys);

        ReleasePO release =
                createRelease(childNamespace, releaseName, releaseComment, configurations, operator);

        //update gray release rules
        GrayReleaseRulePO grayReleaseRule = namespaceBranchService.updateRulesReleaseId(childNamespace.getAppId(),
                parentNamespace.getClusterName(),
                childNamespace.getNamespaceName(),
                childNamespace.getClusterName(),
                release.getId(), operator);

        if (grayReleaseRule != null) {
            releaseOperationContext.put(ReleaseOperationContext.RULES, GrayReleaseRuleItemTransformer
                    .batchTransformFromJSON(grayReleaseRule.getRules()));
        }

        releaseHistoryService.createReleaseHistory(parentNamespace.getAppId(), parentNamespace.getClusterName(),
                parentNamespace.getNamespaceName(), childNamespace.getClusterName(),
                release.getId(),
                previousReleaseId, releaseOperation, releaseOperationContext, operator);

        return release;
    }

    private Map<String, String> mergeConfiguration(Map<String, String> baseConfigurations,
                                                   Map<String, String> coverConfigurations) {
        Map<String, String> result = new HashMap<>();
        //copy base configuration
        for (Map.Entry<String, String> entry : baseConfigurations.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        //update and publish
        for (Map.Entry<String, String> entry : coverConfigurations.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }


    private Map<String, String> getNamespaceItems(NamespacePO namespace) {
        List<ItemPO> items = itemService.findItemsWithoutOrdered(namespace.getId());
        Map<String, String> configurations = new HashMap<>();
        for (ItemPO item : items) {
            if (StringUtils.isEmpty(item.getKey())) {
                continue;
            }
            configurations.put(item.getKey(), item.getValue());
        }

        return configurations;
    }

    private ReleasePO createRelease(NamespacePO namespace, String name, String comment,
                                    Map<String, String> configurations, String operator) {
        ReleasePO release = new ReleasePO();
        release.setReleaseKey(ReleaseKeyGenerator.generateReleaseKey(namespace));
        release.setDataChangeCreatedTime(new Date());
        release.setDataChangeCreatedBy(operator);
        release.setDataChangeLastModifiedBy(operator);
        release.setName(name);
        release.setComment(comment);
        release.setAppId(namespace.getAppId());
        release.setClusterName(namespace.getClusterName());
        release.setNamespaceName(namespace.getNamespaceName());
        release.setConfigurations(gson.toJson(configurations));
        release = releaseRepository.save(release);

        namespaceLockService.unlock(namespace.getId());
        auditService.audit(ReleasePO.class.getSimpleName(), release.getId(), OpAudit.INSERT,
                release.getDataChangeCreatedBy());

        return release;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReleasePO rollback(long releaseId, String operator) {
        ReleasePO release = findOne(releaseId);
        if (release == null) {
            throw new NotFoundException("release not found");
        }
        if (release.isAbandoned()) {
            throw new BadRequestException("release is not active");
        }

        String appId = release.getAppId();
        String clusterName = release.getClusterName();
        String namespaceName = release.getNamespaceName();

        PageRequest page = PageRequest.of(0, 2);
        List<ReleasePO> twoLatestActiveReleases = findActiveReleases(appId, clusterName, namespaceName, page);
        if (twoLatestActiveReleases == null || twoLatestActiveReleases.size() < 2) {
            throw new BadRequestException(String.format(
                    "Can't rollback namespace(appId=%s, clusterName=%s, namespaceName=%s) because there is only one active release",
                    appId,
                    clusterName,
                    namespaceName));
        }

        release.setAbandoned(true);
        release.setDataChangeLastModifiedBy(operator);

        releaseRepository.save(release);

        releaseHistoryService.createReleaseHistory(appId, clusterName,
                namespaceName, clusterName, twoLatestActiveReleases.get(1).getId(),
                release.getId(), ReleaseOperation.ROLLBACK, null, operator);

        //publish child namespace if namespace has child
        rollbackChildNamespace(appId, clusterName, namespaceName, twoLatestActiveReleases, operator);

        return release;
    }

    private void rollbackChildNamespace(String appId, String clusterName, String namespaceName,
                                        List<ReleasePO> parentNamespaceTwoLatestActiveRelease, String operator) {

    }

    private Map<String, String> calculateChildNamespaceToPublishConfiguration(
            Map<String, String> parentNamespaceOldConfiguration, Map<String, String> parentNamespaceNewConfiguration,
            Map<String, String> childNamespaceLatestActiveConfiguration, Collection<String> branchReleaseKeys) {
        //first. calculate child namespace modified configs

        Map<String, String> childNamespaceModifiedConfiguration = calculateBranchModifiedItemsAccordingToRelease(
                parentNamespaceOldConfiguration, childNamespaceLatestActiveConfiguration, branchReleaseKeys);

        //second. append child namespace modified configs to parent namespace new latest configuration
        return mergeConfiguration(parentNamespaceNewConfiguration, childNamespaceModifiedConfiguration);
    }

    private Map<String, String> calculateBranchModifiedItemsAccordingToRelease(
            Map<String, String> masterReleaseConfigs, Map<String, String> branchReleaseConfigs,
            Collection<String> branchReleaseKeys) {

        Map<String, String> modifiedConfigs = new HashMap<>();

        if (CollectionUtils.isEmpty(branchReleaseConfigs)) {
            return modifiedConfigs;
        }

        // new logic, retrieve modified configurations based on branch release keys
        if (branchReleaseKeys != null) {
            for (String branchReleaseKey : branchReleaseKeys) {
                if (branchReleaseConfigs.containsKey(branchReleaseKey)) {
                    modifiedConfigs.put(branchReleaseKey, branchReleaseConfigs.get(branchReleaseKey));
                }
            }

            return modifiedConfigs;
        }

        // old logic, retrieve modified configurations by comparing branchReleaseConfigs with masterReleaseConfigs
        if (CollectionUtils.isEmpty(masterReleaseConfigs)) {
            return branchReleaseConfigs;
        }

        for (Map.Entry<String, String> entry : branchReleaseConfigs.entrySet()) {

            if (!Objects.equals(entry.getValue(), masterReleaseConfigs.get(entry.getKey()))) {
                modifiedConfigs.put(entry.getKey(), entry.getValue());
            }
        }

        return modifiedConfigs;

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchDelete(String appId, String clusterName, String namespaceName, String operator) {
        return releaseRepository.batchDelete(appId, clusterName, namespaceName, operator);
    }

}
