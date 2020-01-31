package lucky.apollo.configservice.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.configservice.cache.AppNamespaceServiceWithCache;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/12/9
 */
@Component
public class WatchKeysUtil {

    private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);

    private final AppNamespaceServiceWithCache appNamespaceService;

    public WatchKeysUtil(final AppNamespaceServiceWithCache appNamespaceService) {
        this.appNamespaceService = appNamespaceService;
    }

    /**
     * Assemble watch keys for the given appId, cluster, namespace, dataCenter combination
     */
    public Set<String> assembleAllWatchKeys(String appId, String clusterName, String namespace,
                                            String dataCenter) {
        Multimap<String, String> watchedKeysMap =
                assembleAllWatchKeys(appId, clusterName, Sets.newHashSet(namespace), dataCenter);
        return Sets.newHashSet(watchedKeysMap.get(namespace));
    }

    /**
     * Assemble watch keys for the given appId, cluster, namespaces, dataCenter combination
     *
     * @return a multimap with namespace as the key and watch keys as the value
     */
    public Multimap<String, String> assembleAllWatchKeys(String appId, String clusterName,
                                                         Set<String> namespaces,
                                                         String dataCenter) {
        return assembleWatchKeys(appId, clusterName, namespaces, dataCenter);
    }

    private Multimap<String, String> assembleWatchKeys(String appId, String clusterName,
                                                       Set<String> namespaces,
                                                       String dataCenter) {
        Multimap<String, String> watchedKeysMap = HashMultimap.create();

        for (String namespace : namespaces) {
            watchedKeysMap
                    .putAll(namespace, assembleWatchKeys(appId, clusterName, namespace, dataCenter));
        }

        return watchedKeysMap;
    }

    private Set<String> assembleWatchKeys(String appId, String clusterName, String namespace,
                                          String dataCenter) {
        if (ConfigConsts.NO_APP_ID_PLACEHOLDER.equalsIgnoreCase(appId)) {
            return Collections.emptySet();
        }
        Set<String> watchedKeys = Sets.newHashSet();

        //watch specified cluster config change
        if (!Objects.equals(ConfigConsts.CLUSTER_NAME_DEFAULT, clusterName)) {
            watchedKeys.add(assembleKey(appId, clusterName, namespace));
        }

        //watch data center config change
        if (!Strings.isNullOrEmpty(dataCenter) && !Objects.equals(dataCenter, clusterName)) {
            watchedKeys.add(assembleKey(appId, dataCenter, namespace));
        }

        //watch default cluster config change
        watchedKeys.add(assembleKey(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace));

        return watchedKeys;
    }

    private String assembleKey(String appId, String cluster, String namespace) {
        return STRING_JOINER.join(appId, cluster, namespace);
    }


}
