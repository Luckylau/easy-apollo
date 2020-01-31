package lucky.apollo.configservice.component;

import lucky.apollo.common.entity.po.AppNamespacePO;
import lucky.apollo.configservice.cache.AppNamespaceServiceWithCache;
import org.springframework.stereotype.Component;

/**
 * @Author luckylau
 * @Date 2019/12/6
 */
@Component
public class NamespaceService {

    private final AppNamespaceServiceWithCache appNamespaceServiceWithCache;

    public NamespaceService(final AppNamespaceServiceWithCache appNamespaceServiceWithCache) {
        this.appNamespaceServiceWithCache = appNamespaceServiceWithCache;
    }

    public String filterNamespaceName(String namespaceName) {
        if (namespaceName != null && namespaceName.toLowerCase().endsWith(".properties")) {
            int dotIndex = namespaceName.lastIndexOf(".");
            return namespaceName.substring(0, dotIndex);
        }

        return namespaceName;
    }

    public String normalizeNamespace(String appId, String namespaceName) {
        AppNamespacePO appNamespace = appNamespaceServiceWithCache.findByAppIdAndNamespace(appId, namespaceName);
        if (appNamespace != null) {
            return appNamespace.getName();
        }

        return namespaceName;
    }
}
