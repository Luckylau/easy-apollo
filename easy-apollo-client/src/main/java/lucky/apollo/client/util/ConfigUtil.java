package lucky.apollo.client.util;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.foundation.Foundation;
import lucky.apollo.client.metaservice.MetaDomainConsts;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.constant.Env;
import lucky.apollo.common.utils.EnvUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @Author luckylau
 * @Date 2019/12/17
 */
@Slf4j
public class ConfigUtil {
    private int refreshInterval = 5;
    private TimeUnit refreshIntervalTimeUnit = TimeUnit.MINUTES;
    /**
     * 1 second
     */
    private int connectTimeout = 1000;
    /**
     * 5 seconds
     */
    private int readTimeout = 5000;
    private String cluster;
    /**
     * 2 times per second
     */
    private int loadConfigQPS = 2;
    /**
     * 2 times per second
     */
    private int longPollQPS = 2;

    /**
     * for on error retry
     * 1 second
     */
    private long onErrorRetryInterval = 1;
    /**
     * 1 second
     */
    private TimeUnit onErrorRetryIntervalTimeUnit = TimeUnit.SECONDS;
    /**
     * for typed config cache of parser result, e.g. integer, double, long, etc.
     * 500 cache key
     */
    private long maxConfigCacheSize = 500;
    /**
     * 1 minute
     */
    private long configCacheExpireTime = 1;
    /**
     * 1 minute
     */
    private TimeUnit configCacheExpireTimeUnit = TimeUnit.MINUTES;
    /**
     *
     */
    private long longPollingInitialDelayInMills = 2000;
    /**
     *
     */
    private boolean autoUpdateInjectedSpringProperties = true;

    private final RateLimiter warnLogRateLimiter;

    public ConfigUtil() {
        /**
         * 1 warning log output per minute
         */
        warnLogRateLimiter = RateLimiter.create(0.017);
        initRefreshInterval();
        initConnectTimeout();
        initReadTimeout();
        initCluster();
        initQPS();
        initMaxConfigCacheSize();
        initLongPollingInitialDelayInMills();
        initAutoUpdateInjectedSpringProperties();
    }

    /**
     * Get the app id for the current application.
     *
     * @return the app id or ConfigConsts.NO_APPID_PLACEHOLDER if app id is not available
     */
    public String getAppId() {
        String appId = Foundation.app().getAppId();
        if (Strings.isNullOrEmpty(appId)) {
            appId = ConfigConsts.NO_APP_ID_PLACEHOLDER;
            if (warnLogRateLimiter.tryAcquire()) {
                log.warn(
                        "app.id is not set, please make sure it is set in classpath:/META-INF/app.properties, now apollo will only load public namespace configurations!");
            }
        }
        return appId;
    }

    /**
     * Get the data center info for the current application.
     *
     * @return the current data center, null if there is no such info.
     */
    public String getDataCenter() {
        return Foundation.server().getDataCenter();
    }

    private void initCluster() {
        //Load data center from system property
        cluster = System.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY);

        //Use data center as cluster
        if (Strings.isNullOrEmpty(cluster)) {
            cluster = getDataCenter();
        }

        //Use default cluster
        if (Strings.isNullOrEmpty(cluster)) {
            cluster = ConfigConsts.CLUSTER_NAME_DEFAULT;
        }
    }

    /**
     * Get the cluster name for the current application.
     *
     * @return the cluster name, or "default" if not specified
     */
    public String getCluster() {
        return cluster;
    }

    /**
     * Get the current environment.
     *
     * @return the env, UNKNOWN if env is not set or invalid
     */
    public Env getApolloEnv() {
        return EnvUtils.transformEnv(Foundation.server().getEnvType());
    }

    public String getLocalIp() {
        return Foundation.net().getHostAddress();
    }

    public String getMetaServerDomainName() {
        return MetaDomainConsts.getDomain(getApolloEnv());
    }

    private void initConnectTimeout() {
        String customizedConnectTimeout = System.getProperty("apollo.connectTimeout");
        if (!Strings.isNullOrEmpty(customizedConnectTimeout)) {
            try {
                connectTimeout = Integer.parseInt(customizedConnectTimeout);
            } catch (Throwable ex) {
                log.error("Config for apollo.connectTimeout is invalid: {}", customizedConnectTimeout);
            }
        }
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    private void initReadTimeout() {
        String customizedReadTimeout = System.getProperty("apollo.readTimeout");
        if (!Strings.isNullOrEmpty(customizedReadTimeout)) {
            try {
                readTimeout = Integer.parseInt(customizedReadTimeout);
            } catch (Throwable ex) {
                log.error("Config for apollo.readTimeout is invalid: {}", customizedReadTimeout);
            }
        }
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    private void initRefreshInterval() {
        String customizedRefreshInterval = System.getProperty("apollo.refreshInterval");
        if (!Strings.isNullOrEmpty(customizedRefreshInterval)) {
            try {
                refreshInterval = Integer.parseInt(customizedRefreshInterval);
            } catch (Throwable ex) {
                log.error("Config for apollo.refreshInterval is invalid: {}", customizedRefreshInterval);
            }
        }
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public TimeUnit getRefreshIntervalTimeUnit() {
        return refreshIntervalTimeUnit;
    }

    private void initQPS() {
        String customizedLoadConfigQPS = System.getProperty("apollo.loadConfigQPS");
        if (!Strings.isNullOrEmpty(customizedLoadConfigQPS)) {
            try {
                loadConfigQPS = Integer.parseInt(customizedLoadConfigQPS);
            } catch (Throwable ex) {
                log.error("Config for apollo.loadConfigQPS is invalid: {}", customizedLoadConfigQPS);
            }
        }

        String customizedLongPollQPS = System.getProperty("apollo.longPollQPS");
        if (!Strings.isNullOrEmpty(customizedLongPollQPS)) {
            try {
                longPollQPS = Integer.parseInt(customizedLongPollQPS);
            } catch (Throwable ex) {
                log.error("Config for apollo.longPollQPS is invalid: {}", customizedLongPollQPS);
            }
        }
    }

    public int getLoadConfigQPS() {
        return loadConfigQPS;
    }

    public int getLongPollQPS() {
        return longPollQPS;
    }

    public long getOnErrorRetryInterval() {
        return onErrorRetryInterval;
    }

    public TimeUnit getOnErrorRetryIntervalTimeUnit() {
        return onErrorRetryIntervalTimeUnit;
    }

    public String getDefaultLocalCacheDir() {
        String cacheRoot = getCustomizedCacheRoot();

        if (!Strings.isNullOrEmpty(cacheRoot)) {
            return cacheRoot + File.separator + getAppId();
        }

        cacheRoot = isOSWindows() ? "C:\\opt\\data\\%s" : "/opt/data/%s";
        return String.format(cacheRoot, getAppId());
    }

    private String getCustomizedCacheRoot() {
        // 1. Get from System Property
        String cacheRoot = System.getProperty("apollo.cacheDir");
        if (Strings.isNullOrEmpty(cacheRoot)) {
            // 2. Get from OS environment variable
            cacheRoot = System.getenv("APOLLO_CACHEDIR");
        }
        if (Strings.isNullOrEmpty(cacheRoot)) {
            // 3. Get from server.properties
            cacheRoot = Foundation.server().getProperty("apollo.cacheDir", null);
        }
        if (Strings.isNullOrEmpty(cacheRoot)) {
            // 4. Get from app.properties
            cacheRoot = Foundation.app().getProperty("apollo.cacheDir", null);
        }

        return cacheRoot;
    }

    public boolean isOSWindows() {
        String osName = System.getProperty("os.name");
        if (Strings.isNullOrEmpty(osName)) {
            return false;
        }
        return osName.startsWith("Windows");
    }

    private void initMaxConfigCacheSize() {
        String customizedConfigCacheSize = System.getProperty("apollo.configCacheSize");
        if (!Strings.isNullOrEmpty(customizedConfigCacheSize)) {
            try {
                maxConfigCacheSize = Long.valueOf(customizedConfigCacheSize);
            } catch (Throwable ex) {
                log.error("Config for apollo.configCacheSize is invalid: {}", customizedConfigCacheSize);
            }
        }
    }

    public long getMaxConfigCacheSize() {
        return maxConfigCacheSize;
    }

    public long getConfigCacheExpireTime() {
        return configCacheExpireTime;
    }

    public TimeUnit getConfigCacheExpireTimeUnit() {
        return configCacheExpireTimeUnit;
    }

    private void initLongPollingInitialDelayInMills() {
        String customizedLongPollingInitialDelay = System.getProperty("apollo.longPollingInitialDelayInMills");
        if (!Strings.isNullOrEmpty(customizedLongPollingInitialDelay)) {
            try {
                longPollingInitialDelayInMills = Long.valueOf(customizedLongPollingInitialDelay);
            } catch (Throwable ex) {
                log.error("Config for apollo.longPollingInitialDelayInMills is invalid: {}", customizedLongPollingInitialDelay);
            }
        }
    }

    public long getLongPollingInitialDelayInMills() {
        return longPollingInitialDelayInMills;
    }

    private void initAutoUpdateInjectedSpringProperties() {
        // 1. Get from System Property
        String enableAutoUpdate = System.getProperty("apollo.autoUpdateInjectedSpringProperties");
        if (Strings.isNullOrEmpty(enableAutoUpdate)) {
            // 2. Get from app.properties
            enableAutoUpdate = Foundation.app().getProperty("apollo.autoUpdateInjectedSpringProperties", null);
        }
        if (!Strings.isNullOrEmpty(enableAutoUpdate)) {
            autoUpdateInjectedSpringProperties = Boolean.parseBoolean(enableAutoUpdate.trim());
        }
    }

    public boolean isAutoUpdateInjectedSpringPropertiesEnabled() {
        return autoUpdateInjectedSpringProperties;
    }
}
