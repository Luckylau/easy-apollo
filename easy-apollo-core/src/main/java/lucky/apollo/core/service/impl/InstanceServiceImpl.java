package lucky.apollo.core.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lucky.apollo.core.entity.InstanceConfigPO;
import lucky.apollo.core.entity.InstancePO;
import lucky.apollo.core.repository.InstanceConfigRepository;
import lucky.apollo.core.repository.InstanceRepository;
import lucky.apollo.core.service.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@Service
public class InstanceServiceImpl implements InstanceService {
    @Autowired
    private InstanceRepository instanceRepository;
    @Autowired
    private InstanceConfigRepository instanceConfigRepository;


    @Override
    public InstancePO findInstance(String appId, String clusterName, String dataCenter, String ip) {
        return instanceRepository.findByAppIdAndClusterNameAndDataCenterAndIp(appId, clusterName,
                dataCenter, ip);
    }

    @Override
    public List<InstancePO> findInstancesByIds(Set<Long> instanceIds) {
        Iterable<InstancePO> instances = instanceRepository.findAllById(instanceIds);
        return Lists.newArrayList(instances);
    }

    @Transactional
    @Override
    public InstancePO createInstance(InstancePO instance) {
        //protection
        instance.setId(0);

        return instanceRepository.save(instance);
    }

    @Override
    public InstanceConfigPO findInstanceConfig(long instanceId, String configAppId, String
            configNamespaceName) {
        return instanceConfigRepository
                .findByInstanceIdAndConfigAppIdAndConfigNamespaceName(
                        instanceId, configAppId, configNamespaceName);
    }

    @Override
    public Page<InstanceConfigPO> findActiveInstanceConfigsByReleaseKey(String releaseKey, Pageable
            pageable) {
        Page<InstanceConfigPO> instanceConfigs = instanceConfigRepository
                .findByReleaseKeyAndDataChangeLastModifiedTimeAfter(releaseKey,
                        getValidInstanceConfigDate(), pageable);
        return instanceConfigs;
    }

    @Override
    public Page<InstancePO> findInstancesByNamespace(String appId, String clusterName, String
            namespaceName, Pageable pageable) {
        Page<InstanceConfigPO> instanceConfigs = instanceConfigRepository.
                findByConfigAppIdAndConfigClusterNameAndConfigNamespaceNameAndDataChangeLastModifiedTimeAfter(appId, clusterName,
                        namespaceName, getValidInstanceConfigDate(), pageable);

        List<InstancePO> instances = Collections.emptyList();
        if (instanceConfigs.hasContent()) {
            Set<Long> instanceIds = instanceConfigs.getContent().stream().map
                    (InstanceConfigPO::getInstanceId).collect(Collectors.toSet());
            instances = findInstancesByIds(instanceIds);
        }

        return new PageImpl<>(instances, pageable, instanceConfigs.getTotalElements());
    }

    @Override
    public Page<InstancePO> findInstancesByNamespaceAndInstanceAppId(String instanceAppId, String
            appId, String clusterName, String
                                                                             namespaceName, Pageable
                                                                             pageable) {
        Page<Object> instanceIdResult = instanceConfigRepository
                .findInstanceIdsByNamespaceAndInstanceAppId(instanceAppId, appId, clusterName,
                        namespaceName, getValidInstanceConfigDate(), pageable);

        List<InstancePO> instances = Collections.emptyList();
        if (instanceIdResult.hasContent()) {
            Set<Long> instanceIds = instanceIdResult.getContent().stream().map((Object o) -> {
                if (o == null) {
                    return null;
                }

                if (o instanceof Integer) {
                    return ((Integer) o).longValue();
                }

                if (o instanceof Long) {
                    return (Long) o;
                }

                //for h2 test
                if (o instanceof BigInteger) {
                    return ((BigInteger) o).longValue();
                }

                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
            instances = findInstancesByIds(instanceIds);
        }

        return new PageImpl<>(instances, pageable, instanceIdResult.getTotalElements());
    }

    @Override
    public List<InstanceConfigPO> findInstanceConfigsByNamespaceWithReleaseKeysNotIn(String appId,
                                                                                     String clusterName,
                                                                                     String
                                                                                             namespaceName,
                                                                                     Set<String>
                                                                                             releaseKeysNotIn) {
        List<InstanceConfigPO> instanceConfigs = instanceConfigRepository.
                findByConfigAppIdAndConfigClusterNameAndConfigNamespaceNameAndDataChangeLastModifiedTimeAfterAndReleaseKeyNotIn(appId, clusterName,
                        namespaceName, getValidInstanceConfigDate(), releaseKeysNotIn);

        if (CollectionUtils.isEmpty(instanceConfigs)) {
            return Collections.emptyList();
        }

        return instanceConfigs;
    }

    /**
     * Currently the instance config is expired by 1 day, add one more hour to avoid possible time
     * difference
     */
    private Date getValidInstanceConfigDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.add(Calendar.HOUR, -1);
        return cal.getTime();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public InstanceConfigPO createInstanceConfig(InstanceConfigPO instanceConfig) {
        instanceConfig.setId(0); //protection

        return instanceConfigRepository.save(instanceConfig);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public InstanceConfigPO updateInstanceConfig(InstanceConfigPO instanceConfig) {
        InstanceConfigPO existedInstanceConfig = instanceConfigRepository.findById(instanceConfig.getId()).orElse(null);
        Preconditions.checkArgument(existedInstanceConfig != null, String.format(
                "Instance config %d doesn't exist", instanceConfig.getId()));

        existedInstanceConfig.setConfigClusterName(instanceConfig.getConfigClusterName());
        existedInstanceConfig.setReleaseKey(instanceConfig.getReleaseKey());
        existedInstanceConfig.setReleaseDeliveryTime(instanceConfig.getReleaseDeliveryTime());
        existedInstanceConfig.setDataChangeLastModifiedTime(instanceConfig
                .getDataChangeLastModifiedTime());

        return instanceConfigRepository.save(existedInstanceConfig);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchDeleteInstanceConfig(String configAppId, String configClusterName, String configNamespaceName) {
        return instanceConfigRepository.batchDelete(configAppId, configClusterName, configNamespaceName);
    }
}
