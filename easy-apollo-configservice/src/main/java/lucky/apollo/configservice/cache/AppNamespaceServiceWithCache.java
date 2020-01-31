package lucky.apollo.configservice.cache;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.entity.po.AppNamespacePO;
import lucky.apollo.common.utils.ApolloThreadFactory;
import lucky.apollo.common.utils.StringUtils;
import lucky.apollo.configservice.wrapper.CaseInsensitiveMapWrapper;
import lucky.apollo.core.config.ServiceConfig;
import lucky.apollo.core.repository.AppNamespaceRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author luckylau
 * @Date 2019/12/6
 */
@Slf4j
@Service
public class AppNamespaceServiceWithCache implements InitializingBean {

    private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
            .skipNulls();
    private final AppNamespaceRepository appNamespaceRepository;
    private final ServiceConfig serviceConfig;

    private int scanInterval;
    private TimeUnit scanIntervalTimeUnit;
    private int rebuildInterval;
    private TimeUnit rebuildIntervalTimeUnit;
    private ScheduledExecutorService scheduledExecutorService;
    private long maxIdScanned;

    /**
     * store appId+namespaceName -> AppNamespace
     */
    private CaseInsensitiveMapWrapper<AppNamespacePO> appNamespaceCache;

    /**
     * store id -> AppNamespace
     */
    private Map<Long, AppNamespacePO> appNamespaceIdCache;

    public AppNamespaceServiceWithCache(
            final AppNamespaceRepository appNamespaceRepository,
            final ServiceConfig serviceConfig) {
        this.appNamespaceRepository = appNamespaceRepository;
        this.serviceConfig = serviceConfig;
        initialize();
    }


    private void initialize() {
        maxIdScanned = 0;
        appNamespaceCache = new CaseInsensitiveMapWrapper<>(Maps.newConcurrentMap());
        appNamespaceIdCache = Maps.newConcurrentMap();
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1, ApolloThreadFactory
                .create("AppNamespaceServiceWithCache", true));
    }


    public AppNamespacePO findByAppIdAndNamespace(String appId, String namespaceName) {
        Preconditions.checkArgument(!StringUtils.isContainEmpty(appId, namespaceName), "appId and namespaceName must not be empty");
        return appNamespaceCache.get(STRING_JOINER.join(appId, namespaceName));
    }

    public List<AppNamespacePO> findByAppIdAndNamespaces(String appId, Set<String> namespaceNames) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(appId), "appId must not be null");
        if (namespaceNames == null || namespaceNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<AppNamespacePO> result = Lists.newArrayList();
        for (String namespaceName : namespaceNames) {
            AppNamespacePO appNamespace = appNamespaceCache.get(STRING_JOINER.join(appId, namespaceName));
            if (appNamespace != null) {
                result.add(appNamespace);
            }
        }
        return result;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        populateDataBaseInterval();
        scanNewAppNamespaces(); //block the startup process until load finished
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                this.updateAndDeleteCache();
            } catch (Throwable ex) {
                log.error("Rebuild cache failed", ex);
            }
        }, rebuildInterval, rebuildInterval, rebuildIntervalTimeUnit);
        scheduledExecutorService.scheduleWithFixedDelay(this::scanNewAppNamespaces, scanInterval,
                scanInterval, scanIntervalTimeUnit);
    }

    private void updateAndDeleteCache() {
        List<Long> ids = Lists.newArrayList(appNamespaceIdCache.keySet());
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<List<Long>> partitionIds = Lists.partition(ids, 500);
        for (List<Long> toRebuild : partitionIds) {
            //这里会自动查询delete=0的情况
            Iterable<AppNamespacePO> appNamespaces = appNamespaceRepository.findAllById(toRebuild);

            if (!appNamespaces.iterator().hasNext()) {
                continue;
            }

            //handle updated
            Set<Long> foundIds = handleUpdatedAppNamespaces(appNamespaces);

            //handle deleted
            //
            handleDeletedAppNamespaces(Sets.difference(Sets.newHashSet(toRebuild), foundIds));
        }
    }

    private void handleDeletedAppNamespaces(Set<Long> deletedIds) {
        if (CollectionUtils.isEmpty(deletedIds)) {
            return;
        }
        for (Long deletedId : deletedIds) {
            AppNamespacePO deleted = appNamespaceIdCache.remove(deletedId);
            if (deleted == null) {
                continue;
            }
            appNamespaceCache.remove(assembleAppNamespaceKey(deleted));
            log.info("Found AppNamespace deleted, {}", deleted);
        }
    }

    private Set<Long> handleUpdatedAppNamespaces(Iterable<AppNamespacePO> appNamespaces) {
        Set<Long> foundIds = Sets.newHashSet();
        for (AppNamespacePO appNamespace : appNamespaces) {
            foundIds.add(appNamespace.getId());
            AppNamespacePO thatInCache = appNamespaceIdCache.get(appNamespace.getId());
            if (thatInCache != null && appNamespace.getDataChangeLastModifiedTime().after(thatInCache
                    .getDataChangeLastModifiedTime())) {
                appNamespaceIdCache.put(appNamespace.getId(), appNamespace);
                String oldKey = assembleAppNamespaceKey(thatInCache);
                String newKey = assembleAppNamespaceKey(appNamespace);
                appNamespaceCache.put(newKey, appNamespace);

                //in case appId or namespaceName changes
                if (!newKey.equals(oldKey)) {
                    appNamespaceCache.remove(oldKey);
                }

                log.info("Found AppNamespace changes, old: {}, new: {}", thatInCache, appNamespace);
            }
        }
        return foundIds;
    }

    private void populateDataBaseInterval() {
        scanInterval = serviceConfig.appNamespaceCacheScanInterval();
        scanIntervalTimeUnit = serviceConfig.appNamespaceCacheScanIntervalTimeUnit();
        rebuildInterval = serviceConfig.appNamespaceCacheRebuildInterval();
        rebuildIntervalTimeUnit = serviceConfig.appNamespaceCacheRebuildIntervalTimeUnit();
    }

    private void scanNewAppNamespaces() {
        try {
            this.loadNewAppNamespaces();
        } catch (Throwable ex) {
            log.error("Load new app namespaces failed", ex);
        }
    }

    private void loadNewAppNamespaces() {
        boolean hasMore = true;
        while (hasMore && !Thread.currentThread().isInterrupted()) {
            //current batch is 500
            List<AppNamespacePO> appNamespaces = appNamespaceRepository
                    .findFirst500ByIdGreaterThanOrderByIdAsc(maxIdScanned);
            if (CollectionUtils.isEmpty(appNamespaces)) {
                break;
            }
            mergeAppNamespaces(appNamespaces);
            int scanned = appNamespaces.size();
            maxIdScanned = appNamespaces.get(scanned - 1).getId();
            hasMore = scanned == 500;
            log.info("Loaded {} new app namespaces with startId {}", scanned, maxIdScanned);
        }
    }

    private void mergeAppNamespaces(List<AppNamespacePO> appNamespaces) {
        for (AppNamespacePO appNamespace : appNamespaces) {
            appNamespaceCache.put(assembleAppNamespaceKey(appNamespace), appNamespace);
            appNamespaceIdCache.put(appNamespace.getId(), appNamespace);
        }
    }

    private String assembleAppNamespaceKey(AppNamespacePO appNamespace) {
        return STRING_JOINER.join(appNamespace.getAppId(), appNamespace.getName());
    }


}
