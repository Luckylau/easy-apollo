package lucky.apollo.core.config;

import com.google.common.base.Strings;
import lucky.apollo.common.config.RefreshableConfig;
import lucky.apollo.common.config.RefreshablePropertySource;
import lucky.apollo.core.component.ServicePropertySourceRefresher;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Service
public class ServiceConfig extends RefreshableConfig {

    private final ServicePropertySourceRefresher servicePropertySourceRefresher;

    /**
     * 1s
     */
    private static final int DEFAULT_APP_NAMESPACE_CACHE_SCAN_INTERVAL = 1;

    /**
     * 60s
     */
    private static final int DEFAULT_APP_NAMESPACE_CACHE_REBUILD_INTERVAL = 60;


    /**
     * 60s
     */
    private static final int DEFAULT_GRAY_RELEASE_RULE_SCAN_INTERVAL = 60;


    /**
     * 1s
     */
    private static final int DEFAULT_RELEASE_MESSAGE_CACHE_SCAN_INTERVAL = 1;


    private static final int DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH = 100;


    /**
     * 100ms
     */
    private static final int DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH_INTERVAL_IN_MILLI = 100;


    public ServiceConfig(ServicePropertySourceRefresher servicePropertySourceRefresher) {
        this.servicePropertySourceRefresher = servicePropertySourceRefresher;
    }

    @Override
    protected RefreshablePropertySource getRefreshablePropertySource() {
        return servicePropertySourceRefresher;
    }

    public List<String> eurekaServiceUrls() {
        String configuration = getValue("eureka.service.url", "");
        if (Strings.isNullOrEmpty(configuration)) {
            return Collections.emptyList();
        }

        return splitter.splitToList(configuration);
    }


    public boolean isConfigServiceCacheEnabled() {
        return getBooleanProperty("config-service.cache.enabled", false);
    }

    public int appNamespaceCacheScanInterval() {
        int interval = getIntProperty("apollo.app-namespace-cache-scan.interval", DEFAULT_APP_NAMESPACE_CACHE_SCAN_INTERVAL);
        return checkInt(interval, 1, Integer.MAX_VALUE, DEFAULT_APP_NAMESPACE_CACHE_SCAN_INTERVAL);
    }

    public TimeUnit appNamespaceCacheScanIntervalTimeUnit() {
        return TimeUnit.SECONDS;
    }

    public TimeUnit appNamespaceCacheRebuildIntervalTimeUnit() {
        return TimeUnit.SECONDS;
    }

    public int appNamespaceCacheRebuildInterval() {
        int interval = getIntProperty("apollo.app-namespace-cache-rebuild.interval", DEFAULT_APP_NAMESPACE_CACHE_REBUILD_INTERVAL);
        return checkInt(interval, 1, Integer.MAX_VALUE, DEFAULT_APP_NAMESPACE_CACHE_REBUILD_INTERVAL);
    }

    public int grayReleaseRuleScanInterval() {
        int interval = getIntProperty("apollo.gray-release-rule-scan.interval", DEFAULT_GRAY_RELEASE_RULE_SCAN_INTERVAL);
        return checkInt(interval, 1, Integer.MAX_VALUE, DEFAULT_GRAY_RELEASE_RULE_SCAN_INTERVAL);
    }

    public int releaseMessageCacheScanInterval() {
        int interval = getIntProperty("apollo.release-message-cache-scan.interval", DEFAULT_RELEASE_MESSAGE_CACHE_SCAN_INTERVAL);
        return checkInt(interval, 1, Integer.MAX_VALUE, DEFAULT_RELEASE_MESSAGE_CACHE_SCAN_INTERVAL);
    }

    public TimeUnit releaseMessageCacheScanIntervalTimeUnit() {
        return TimeUnit.SECONDS;
    }

    public int releaseMessageNotificationBatch() {
        int batch = getIntProperty("apollo.release-message.notification.batch", DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH);
        return checkInt(batch, 1, Integer.MAX_VALUE, DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH);
    }

    public int releaseMessageNotificationBatchIntervalInMilli() {
        int interval = getIntProperty("apollo.release-message.notification.batch.interval", DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH_INTERVAL_IN_MILLI);
        return checkInt(interval, 10, Integer.MAX_VALUE, DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH_INTERVAL_IN_MILLI);
    }


    private int checkInt(int value, int min, int max, int defaultValue) {
        if (value >= min && value <= max) {
            return value;
        }
        return defaultValue;
    }
}