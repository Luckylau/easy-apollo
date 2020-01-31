package lucky.apollo.core.service.impl;

import com.google.gson.Gson;
import lucky.apollo.common.constant.GsonType;
import lucky.apollo.common.entity.po.AppNamespacePO;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.constant.OpAudit;
import lucky.apollo.core.entity.ClusterPO;
import lucky.apollo.core.entity.ItemPO;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.entity.ReleasePO;
import lucky.apollo.core.message.MessageSender;
import lucky.apollo.core.message.MessageTopic;
import lucky.apollo.core.repository.NamespaceRepository;
import lucky.apollo.core.service.*;
import lucky.apollo.core.utils.ReleaseMessageKeyGenerator;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Service
public class NamespaceServiceImpl implements NamespaceService {

    private Gson gson = new Gson();

    @Autowired
    private NamespaceRepository namespaceRepository;

    @Autowired
    private AppNamespaceService appNamespaceService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private CommitService commitService;

    @Autowired
    private ReleaseHistoryService releaseHistoryService;

    @Autowired
    private ReleaseService releaseService;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private NamespaceLockService namespaceLockService;

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private ClusterService clusterService;


    @Override
    public NamespacePO findOne(Long namespaceId) {
        return namespaceRepository.findById(namespaceId).orElse(null);
    }

    @Override
    public NamespacePO findOne(String appId, String clusterName, String namespaceName) {
        return namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, clusterName,
                namespaceName);
    }


    @Override
    public List<NamespacePO> findNamespaces(String appId, String clusterName) {
        List<NamespacePO> namespaces = namespaceRepository.findByAppIdAndClusterNameOrderByIdAsc(appId, clusterName);
        if (namespaces == null) {
            return Collections.emptyList();
        }
        return namespaces;
    }

    @Override
    public NamespacePO findChildNamespace(String appId, String parentClusterName, String namespaceName) {
        List<NamespacePO> namespaces = findByAppIdAndNamespaceName(appId, namespaceName);
        if (CollectionUtils.isEmpty(namespaces) || namespaces.size() == 1) {
            return null;
        }

        List<ClusterPO> childClusters = clusterService.findChildClusters(appId, parentClusterName);
        if (CollectionUtils.isEmpty(childClusters)) {
            return null;
        }

        Set<String> childClusterNames = childClusters.stream().map(ClusterPO::getName).collect(Collectors.toSet());
        //the child namespace is the intersection of the child clusters and child namespaces
        for (NamespacePO namespace : namespaces) {
            if (childClusterNames.contains(namespace.getClusterName())) {
                return namespace;
            }
        }

        return null;
    }

    @Override
    public List<NamespacePO> findByAppIdAndNamespaceName(String appId, String namespaceName) {
        return namespaceRepository.findByAppIdAndNamespaceNameOrderByIdAsc(appId, namespaceName);
    }


    public boolean isNamespaceUnique(String appId, String cluster, String namespace) {
        Objects.requireNonNull(appId, "AppId must not be null");
        Objects.requireNonNull(cluster, "Cluster must not be null");
        Objects.requireNonNull(namespace, "Namespace must not be null");
        return Objects.isNull(
                namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, cluster, namespace));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByAppIdAndClusterName(String appId, String clusterName, String operator) {

        List<NamespacePO> toDeleteNamespaces = findNamespaces(appId, clusterName);

        for (NamespacePO namespace : toDeleteNamespaces) {

            deleteNamespace(namespace, operator);

        }
    }

    @Override
    public NamespacePO findParentNamespace(NamespacePO namespacePO) {
        String appId = namespacePO.getAppId();
        String namespaceName = namespacePO.getNamespaceName();

        ClusterPO cluster = clusterService.findOne(appId, namespacePO.getClusterName());
        if (cluster != null && cluster.getParentClusterId() > 0) {
            ClusterPO parentCluster = clusterService.findOne(cluster.getParentClusterId());
            return findOne(appId, parentCluster.getName(), namespaceName);
        }

        return null;

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public NamespacePO deleteNamespace(NamespacePO namespace, String operator) {
        String appId = namespace.getAppId();
        String clusterName = namespace.getClusterName();
        String namespaceName = namespace.getNamespaceName();

        itemService.batchDelete(namespace.getId(), operator);
        commitService.batchDelete(appId, clusterName, namespace.getNamespaceName(), operator);

        releaseHistoryService.batchDelete(appId, clusterName, namespaceName, operator);

        instanceService.batchDeleteInstanceConfig(appId, clusterName, namespaceName);

        namespaceLockService.unlock(namespace.getId());

        namespace.setDeleted(true);
        namespace.setDataChangeLastModifiedBy(operator);

        auditService.audit(NamespacePO.class.getSimpleName(), namespace.getId(), OpAudit.DELETE, operator);

        NamespacePO deleted = namespaceRepository.save(namespace);

        //Publish release message to do some clean up in config service, such as updating the cache
        messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName),
                MessageTopic.APOLLO_RELEASE_TOPIC);

        return deleted;
    }

    @Transactional
    @Override
    public NamespacePO save(NamespacePO entity) {
        if (!isNamespaceUnique(entity.getAppId(), entity.getClusterName(), entity.getNamespaceName())) {
            throw new ServiceException("namespace not unique");
        }
        //protection
        entity.setId(0);
        NamespacePO namespace = namespaceRepository.save(entity);

        auditService.audit(NamespacePO.class.getSimpleName(), namespace.getId(), OpAudit.INSERT,
                namespace.getDataChangeCreatedBy());

        return namespace;
    }

    @Transactional
    @Override
    public NamespacePO update(NamespacePO namespace) {
        NamespacePO managedNamespace = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(
                namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName());
        BeanUtils.copyPropertiesWithIgnore(namespace, managedNamespace);
        managedNamespace = namespaceRepository.save(managedNamespace);

        auditService.audit(NamespacePO.class.getSimpleName(), managedNamespace.getId(), OpAudit.UPDATE,
                managedNamespace.getDataChangeLastModifiedBy());

        return managedNamespace;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void instanceOfAppNamespaces(String appId, String clusterName, String createBy) {

        List<AppNamespacePO> appNamespaces = appNamespaceService.findByAppId(appId);

        for (AppNamespacePO appNamespace : appNamespaces) {
            NamespacePO ns = new NamespacePO();
            ns.setAppId(appId);
            ns.setClusterName(clusterName);
            ns.setNamespaceName(appNamespace.getName());
            ns.setDataChangeCreatedBy(createBy);
            ns.setDataChangeLastModifiedBy(createBy);
            namespaceRepository.save(ns);
            auditService.audit(NamespacePO.class.getSimpleName(), ns.getId(), OpAudit.INSERT, createBy);
        }

    }


    private boolean isNamespaceNotPublished(NamespacePO namespace) {
        ReleasePO latestRelease = releaseService.findLatestActiveRelease(namespace);
        long namespaceId = namespace.getId();

        if (latestRelease == null) {
            ItemPO lastItem = itemService.findLastOne(namespaceId);
            return lastItem != null;
        }

        Date lastPublishTime = latestRelease.getDataChangeLastModifiedTime();
        List<ItemPO> itemsModifiedAfterLastPublish = itemService.findItemsModifiedAfterDate(namespaceId, lastPublishTime);

        if (CollectionUtils.isEmpty(itemsModifiedAfterLastPublish)) {
            return false;
        }

        Map<String, String> publishedConfiguration = gson.fromJson(latestRelease.getConfigurations(), GsonType.CONFIG);
        for (ItemPO item : itemsModifiedAfterLastPublish) {
            if (!Objects.equals(item.getValue(), publishedConfiguration.get(item.getKey()))) {
                return true;
            }
        }

        return false;
    }

}
