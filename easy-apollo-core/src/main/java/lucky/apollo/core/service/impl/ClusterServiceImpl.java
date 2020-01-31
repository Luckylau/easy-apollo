package lucky.apollo.core.service.impl;

import com.google.common.base.Strings;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.constant.OpAudit;
import lucky.apollo.core.entity.ClusterPO;
import lucky.apollo.core.repository.ClusterRepository;
import lucky.apollo.core.service.AuditService;
import lucky.apollo.core.service.ClusterService;
import lucky.apollo.core.service.NamespaceService;
import org.hibernate.service.spi.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @Author luckylau
 * @Date 2019/10/11
 */
@Service
public class ClusterServiceImpl implements ClusterService {

    private final ClusterRepository clusterRepository;
    private final AuditService auditService;
    private final NamespaceService namespaceService;

    public ClusterServiceImpl(
            final ClusterRepository clusterRepository,
            final AuditService auditService,
            final @Lazy NamespaceService namespaceService) {
        this.clusterRepository = clusterRepository;
        this.auditService = auditService;
        this.namespaceService = namespaceService;
    }

    @Override
    public boolean isClusterNameUnique(String appId, String clusterName) {
        Objects.requireNonNull(appId, "AppId must not be null");
        Objects.requireNonNull(clusterName, "ClusterName must not be null");
        return Objects.isNull(clusterRepository.findByAppIdAndName(appId, clusterName));
    }

    @Override
    public ClusterPO findOne(String appId, String name) {
        return clusterRepository.findByAppIdAndName(appId, name);
    }

    @Override
    public ClusterPO findOne(long clusterId) {
        return clusterRepository.findById(clusterId).orElse(null);
    }

    @Override
    public List<ClusterPO> findParentClusters(String appId) {
        if (Strings.isNullOrEmpty(appId)) {
            return Collections.emptyList();
        }

        List<ClusterPO> clusters = clusterRepository.findByAppIdAndParentClusterId(appId, 0L);
        if (clusters == null) {
            return Collections.emptyList();
        }

        Collections.sort(clusters);

        return clusters;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ClusterPO saveWithInstanceOfAppNamespaces(ClusterPO entity) {
        ClusterPO savedCluster = saveWithoutInstanceOfAppNamespaces(entity);

        namespaceService.instanceOfAppNamespaces(savedCluster.getAppId(), savedCluster.getName(),
                savedCluster.getDataChangeCreatedBy());

        return savedCluster;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ClusterPO saveWithoutInstanceOfAppNamespaces(ClusterPO entity) {
        if (!isClusterNameUnique(entity.getAppId(), entity.getName())) {
            throw new BadRequestException("cluster not unique");
        }
        //protection
        entity.setId(0);
        ClusterPO cluster = clusterRepository.save(entity);

        auditService.audit(ClusterPO.class.getSimpleName(), cluster.getId(), OpAudit.INSERT,
                cluster.getDataChangeCreatedBy());

        return cluster;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(long id, String operator) {
        ClusterPO cluster = clusterRepository.findById(id).orElse(null);
        if (cluster == null) {
            throw new BadRequestException("cluster not exist");
        }

        //delete linked namespaces
        namespaceService.deleteByAppIdAndClusterName(cluster.getAppId(), cluster.getName(), operator);

        cluster.setDeleted(true);
        cluster.setDataChangeLastModifiedBy(operator);
        clusterRepository.save(cluster);

        auditService.audit(ClusterPO.class.getSimpleName(), id, OpAudit.DELETE, operator);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ClusterPO update(ClusterPO cluster) {
        ClusterPO managedCluster =
                clusterRepository.findByAppIdAndName(cluster.getAppId(), cluster.getName());
        BeanUtils.copyPropertiesWithIgnore(cluster, managedCluster);
        managedCluster = clusterRepository.save(managedCluster);

        auditService.audit(ClusterPO.class.getSimpleName(), managedCluster.getId(), OpAudit.UPDATE,
                managedCluster.getDataChangeLastModifiedBy());

        return managedCluster;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createDefaultCluster(String appId, String createBy) {
        if (!isClusterNameUnique(appId, ConfigConsts.CLUSTER_NAME_DEFAULT)) {
            throw new ServiceException("cluster not unique");
        }
        ClusterPO cluster = new ClusterPO();
        cluster.setName(ConfigConsts.CLUSTER_NAME_DEFAULT);
        cluster.setAppId(appId);
        cluster.setDataChangeCreatedBy(createBy);
        cluster.setDataChangeLastModifiedBy(createBy);
        clusterRepository.save(cluster);

        auditService.audit(ClusterPO.class.getSimpleName(), cluster.getId(), OpAudit.INSERT, createBy);
    }

    @Override
    public List<ClusterPO> findChildClusters(String appId, String parentClusterName) {
        ClusterPO parentCluster = findOne(appId, parentClusterName);
        if (parentCluster == null) {
            throw new BadRequestException("parent cluster not exist");
        }

        return clusterRepository.findByParentClusterId(parentCluster.getId());
    }

    @Override
    public List<ClusterPO> findClusters(String appId) {
        List<ClusterPO> clusters = clusterRepository.findByAppId(appId);

        if (clusters == null) {
            return Collections.emptyList();
        }

        // to make sure parent cluster is ahead of branch cluster
        Collections.sort(clusters);

        return clusters;
    }
}
