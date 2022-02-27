package lucky.apollo.client.config.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.build.ApolloInjector;
import lucky.apollo.client.config.AbstractConfigRepository;
import lucky.apollo.client.config.ConfigRepository;
import lucky.apollo.client.config.ConfigServiceLocator;
import lucky.apollo.client.config.SchedulePolicy;
import lucky.apollo.client.constant.ConfigSourceType;
import lucky.apollo.client.exception.ApolloConfigException;
import lucky.apollo.client.exception.ApolloConfigStatusCodeException;
import lucky.apollo.client.service.RemoteConfigLongPollService;
import lucky.apollo.client.util.ConfigUtil;
import lucky.apollo.client.util.http.HttpRequest;
import lucky.apollo.client.util.http.HttpResponse;
import lucky.apollo.client.util.http.HttpUtil;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.entity.dto.ApolloConfigDTO;
import lucky.apollo.common.entity.dto.ApolloNotificationMessageDTO;
import lucky.apollo.common.entity.dto.ServiceDTO;
import lucky.apollo.common.utils.ApolloThreadFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author luckylau
 * @Date 2020/9/20
 */
@Slf4j
public class RemoteConfigRepository extends AbstractConfigRepository {

    private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
    private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");
    private static final Escaper pathEscaper = UrlEscapers.urlPathSegmentEscaper();
    private static final Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();
    private final ConfigServiceLocator m_serviceLocator;
    private final HttpUtil m_httpUtil;
    private final ConfigUtil m_configUtil;
    private final RemoteConfigLongPollService remoteConfigLongPollService;
    private volatile AtomicReference<ApolloConfigDTO> m_configCache;
    private final String m_namespace;
    private final static ScheduledExecutorService m_executorService;
    private final AtomicReference<ServiceDTO> m_longPollServiceDto;
    private final AtomicReference<ApolloNotificationMessageDTO> m_remoteMessages;
    private final RateLimiter m_loadConfigRateLimiter;
    private final AtomicBoolean m_configNeedForceRefresh;
    private final SchedulePolicy m_loadConfigFailSchedulePolicy;
    private final Gson gson;

    static {
        m_executorService = new ScheduledThreadPoolExecutor(1,
                ApolloThreadFactory.create("RemoteConfigRepository", true));
    }

    /**
     * Constructor.
     *
     * @param namespace the namespace
     */
    public RemoteConfigRepository(String namespace) {
        m_namespace = namespace;
        m_configCache = new AtomicReference<>();
        m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
        m_httpUtil = ApolloInjector.getInstance(HttpUtil.class);
        m_serviceLocator = ApolloInjector.getInstance(ConfigServiceLocator.class);
        remoteConfigLongPollService = ApolloInjector.getInstance(RemoteConfigLongPollService.class);
        m_longPollServiceDto = new AtomicReference<>();
        m_remoteMessages = new AtomicReference<>();
        m_loadConfigRateLimiter = RateLimiter.create(m_configUtil.getLoadConfigQPS());
        m_configNeedForceRefresh = new AtomicBoolean(true);
        m_loadConfigFailSchedulePolicy = new ExponentialSchedulePolicy(m_configUtil.getOnErrorRetryInterval(),
                m_configUtil.getOnErrorRetryInterval() * 8);
        gson = new Gson();
        this.trySync();
        this.schedulePeriodicRefresh();
        this.scheduleLongPollingRefresh();
    }

    private void schedulePeriodicRefresh() {
        log.debug("Schedule periodic refresh with interval: {} {}",
                m_configUtil.getRefreshInterval(), m_configUtil.getRefreshIntervalTimeUnit());
        m_executorService.scheduleAtFixedRate(
                () -> {
                    log.debug("refresh config for namespace: {}", m_namespace);
                    trySync();
                }, m_configUtil.getRefreshInterval(), m_configUtil.getRefreshInterval(),
                m_configUtil.getRefreshIntervalTimeUnit());
    }

    private void scheduleLongPollingRefresh() {
        remoteConfigLongPollService.submit(m_namespace, this);
    }

    @Override
    protected void sync() {
        try {
            ApolloConfigDTO previous = m_configCache.get();
            ApolloConfigDTO current = loadApolloConfig();

            //reference equals means HTTP 304
            if (previous != current) {
                log.debug("Remote Config refreshed!");
                m_configCache.set(current);
                this.fireRepositoryChange(m_namespace, this.getConfig());
            }

            if (current != null) {
                log.info(String.format("Apollo.Client.Configs.%s", current.getNamespaceName()),
                        current.getReleaseKey());
            }

        } catch (Throwable ex) {
            throw ex;
        }
    }

    private ApolloConfigDTO loadApolloConfig() {
        if (!m_loadConfigRateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
            //wait at most 5 seconds
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
            }
        }
        String appId = m_configUtil.getAppId();
        String cluster = m_configUtil.getCluster();
        String dataCenter = m_configUtil.getDataCenter();
        int maxRetries = m_configNeedForceRefresh.get() ? 2 : 1;
        // 0 means no sleep
        long onErrorSleepTime = 0;
        Throwable exception = null;

        List<ServiceDTO> configServices = getConfigServices();
        String url = null;
        for (int i = 0; i < maxRetries; i++) {
            List<ServiceDTO> randomConfigServices = Lists.newLinkedList(configServices);
            Collections.shuffle(randomConfigServices);
            //Access the server which notifies the client first
            if (m_longPollServiceDto.get() != null) {
                randomConfigServices.add(0, m_longPollServiceDto.getAndSet(null));
            }

            for (ServiceDTO configService : randomConfigServices) {
                if (onErrorSleepTime > 0) {
                    log.warn(
                            "Load config failed, will retry in {} {}. appId: {}, cluster: {}, namespaces: {}",
                            onErrorSleepTime, m_configUtil.getOnErrorRetryIntervalTimeUnit(), appId, cluster, m_namespace);

                    try {
                        m_configUtil.getOnErrorRetryIntervalTimeUnit().sleep(onErrorSleepTime);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }

                url = assembleQueryConfigUrl(configService.getHomepageUrl(), appId, cluster, m_namespace,
                        dataCenter, m_remoteMessages.get(), m_configCache.get());

                log.debug("Loading config from {}", url);
                HttpRequest request = new HttpRequest(url);
                try {

                    HttpResponse<ApolloConfigDTO> response = m_httpUtil.doGet(request, ApolloConfigDTO.class);
                    m_configNeedForceRefresh.set(false);
                    m_loadConfigFailSchedulePolicy.success();

                    if (response.getStatusCode() == 304) {
                        log.debug("Config server responds with 304 HTTP status code.");
                        return m_configCache.get();
                    }

                    ApolloConfigDTO result = response.getBody();

                    log.debug("Loaded config for {}: {}", m_namespace, result);

                    return result;
                } catch (ApolloConfigStatusCodeException ex) {
                    ApolloConfigStatusCodeException statusCodeException = ex;
                    //config not found
                    if (ex.getStatusCode() == 404) {
                        String message = String.format(
                                "Could not find config for namespace - appId: %s, cluster: %s, namespace: %s, " +
                                        "please check whether the configs are released in Apollo!",
                                appId, cluster, m_namespace);
                        statusCodeException = new ApolloConfigStatusCodeException(ex.getStatusCode(),
                                message);
                    }
                    exception = statusCodeException;
                } catch (Throwable ex) {
                    log.error("loadApolloConfig error",ex);
                }

                // if force refresh, do normal sleep, if normal config load, do exponential sleep
                onErrorSleepTime = m_configNeedForceRefresh.get() ? m_configUtil.getOnErrorRetryInterval() :
                        m_loadConfigFailSchedulePolicy.fail();
            }

        }
        String message = String.format(
                "Load Apollo Config failed - appId: %s, cluster: %s, namespace: %s, url: %s",
                appId, cluster, m_namespace, url);
        throw new ApolloConfigException(message, exception);
    }

    String assembleQueryConfigUrl(String uri, String appId, String cluster, String namespace,
                                  String dataCenter, ApolloNotificationMessageDTO remoteMessages, ApolloConfigDTO previousConfig) {

        String path = "configs/%s/%s/%s";
        List<String> pathParams =
                Lists.newArrayList(pathEscaper.escape(appId), pathEscaper.escape(cluster),
                        pathEscaper.escape(namespace));
        Map<String, String> queryParams = Maps.newHashMap();

        if (previousConfig != null) {
            queryParams.put("releaseKey", queryParamEscaper.escape(previousConfig.getReleaseKey()));
        }

        if (!Strings.isNullOrEmpty(dataCenter)) {
            queryParams.put("dataCenter", queryParamEscaper.escape(dataCenter));
        }

        String localIp = m_configUtil.getLocalIp();
        if (!Strings.isNullOrEmpty(localIp)) {
            queryParams.put("ip", queryParamEscaper.escape(localIp));
        }

        if (remoteMessages != null) {
            queryParams.put("messages", queryParamEscaper.escape(gson.toJson(remoteMessages)));
        }

        String pathExpanded = String.format(path, pathParams.toArray());

        if (!queryParams.isEmpty()) {
            pathExpanded += "?" + MAP_JOINER.join(queryParams);
        }
        if (!uri.endsWith("/")) {
            uri += "/";
        }
        return uri + pathExpanded;
    }

    private List<ServiceDTO> getConfigServices() {
        List<ServiceDTO> services = m_serviceLocator.getConfigServices();
        if (services.size() == 0) {
            throw new ApolloConfigException("No available config service");
        }

        return services;
    }

    @Override
    public Properties getConfig() {
        if (m_configCache.get() == null) {
            this.sync();
        }
        return transformApolloConfigToProperties(m_configCache.get());
    }

    private Properties transformApolloConfigToProperties(ApolloConfigDTO apolloConfig) {
        Properties result = new Properties();
        result.putAll(apolloConfig.getConfigurations());
        return result;
    }

    @Override
    public void setUpstreamRepository(ConfigRepository upstreamConfigRepository) {
        //remote config doesn't need upstream
    }

    @Override
    public ConfigSourceType getSourceType() {
        return ConfigSourceType.REMOTE;
    }

    public void onLongPollNotified(ServiceDTO longPollNotifiedServiceDto, ApolloNotificationMessageDTO remoteMessages) {
        m_longPollServiceDto.set(longPollNotifiedServiceDto);
        m_remoteMessages.set(remoteMessages);
        m_executorService.submit(() -> {
            m_configNeedForceRefresh.set(true);
            trySync();
        });
    }
}
