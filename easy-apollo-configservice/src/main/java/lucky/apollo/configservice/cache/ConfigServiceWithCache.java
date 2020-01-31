package lucky.apollo.configservice.cache;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.entity.dto.ApolloNotificationMessageDTO;
import lucky.apollo.configservice.service.impl.AbstractConfigService;
import lucky.apollo.core.entity.ReleaseMessagePO;
import lucky.apollo.core.entity.ReleasePO;
import lucky.apollo.core.message.MessageTopic;
import lucky.apollo.core.service.ReleaseMessageService;
import lucky.apollo.core.service.ReleaseService;
import lucky.apollo.core.utils.ReleaseMessageKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @Author luckylau
 * @Date 2019/12/6
 */
@Slf4j
public class ConfigServiceWithCache extends AbstractConfigService {

    /**
     * 1 hour
     */
    private static final long DEFAULT_EXPIRED_AFTER_ACCESS_IN_MINUTES = 60;

    private static final Splitter STRING_SPLITTER =
            Splitter.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR).omitEmptyStrings();

    @Autowired
    private ReleaseService releaseService;

    @Autowired
    private ReleaseMessageService releaseMessageService;

    private LoadingCache<String, ConfigCacheEntry> configCache;

    private LoadingCache<Long, Optional<ReleasePO>> configIdCache;

    private ConfigCacheEntry nullConfigCacheEntry;


    public ConfigServiceWithCache() {
        nullConfigCacheEntry = new ConfigCacheEntry(ConfigConsts.NOTIFICATION_ID_PLACEHOLDER, null);
    }

    @PostConstruct
    private void initialize() {
        configCache = CacheBuilder.newBuilder()
                .expireAfterAccess(DEFAULT_EXPIRED_AFTER_ACCESS_IN_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<String, ConfigCacheEntry>() {
                    @Override
                    public ConfigCacheEntry load(String key) {
                        List<String> namespaceInfo = STRING_SPLITTER.splitToList(key);
                        if (namespaceInfo.size() != 3) {
                            log.error("Invalid cache load key %s", key);
                            return nullConfigCacheEntry;
                        }

                        try {
                            ReleaseMessagePO latestReleaseMessage = releaseMessageService.findLatestReleaseMessageForMessages(Lists
                                    .newArrayList(key));
                            ReleasePO latestRelease = releaseService.findLatestActiveRelease(namespaceInfo.get(0), namespaceInfo.get(1),
                                    namespaceInfo.get(2));

                            long notificationId = latestReleaseMessage == null ? ConfigConsts.NOTIFICATION_ID_PLACEHOLDER : latestReleaseMessage
                                    .getId();

                            if (notificationId == ConfigConsts.NOTIFICATION_ID_PLACEHOLDER && latestRelease == null) {
                                return nullConfigCacheEntry;
                            }

                            return new ConfigCacheEntry(notificationId, latestRelease);
                        } catch (Throwable ex) {
                            throw ex;
                        }
                    }
                });

        configIdCache = CacheBuilder.newBuilder()
                .expireAfterAccess(DEFAULT_EXPIRED_AFTER_ACCESS_IN_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<Long, Optional<ReleasePO>>() {
                    @Override
                    public Optional<ReleasePO> load(Long key) {
                        try {
                            ReleasePO release = releaseService.findActiveOne(key);
                            return Optional.ofNullable(release);
                        } catch (Throwable ex) {
                            throw ex;
                        }
                    }
                });
    }

    @Override
    protected ReleasePO findActiveOne(long id, ApolloNotificationMessageDTO clientMessages) {
        return configIdCache.getUnchecked(id).orElse(null);
    }

    @Override
    protected ReleasePO findLatestActiveRelease(String configAppId, String configClusterName, String configNamespaceName, ApolloNotificationMessageDTO clientMessages) {
        String key = ReleaseMessageKeyGenerator.generate(configAppId, configClusterName, configNamespaceName);


        ConfigCacheEntry cacheEntry = configCache.getUnchecked(key);

        //cache is out-dated
        if (clientMessages != null && clientMessages.has(key) &&
                clientMessages.get(key) > cacheEntry.getNotificationId()) {
            //invalidate the cache and try to load from db again
            invalidate(key);
            cacheEntry = configCache.getUnchecked(key);
        }

        return cacheEntry.getRelease();
    }

    @Override
    public void handleMessage(ReleaseMessagePO message, String channel) {
        log.info("message received - channel: {}, message: {}", channel, message);
        if (!MessageTopic.APOLLO_RELEASE_TOPIC.equals(channel) || Strings.isNullOrEmpty(message.getMessage())) {
            return;
        }

        try {
            invalidate(message.getMessage());
            //warm up the cache
            configCache.getUnchecked(message.getMessage());
        } catch (Throwable ex) {
            //ignore
        }
    }

    private void invalidate(String key) {
        configCache.invalidate(key);
    }

    private static class ConfigCacheEntry {
        private final long notificationId;
        private final ReleasePO release;

        public ConfigCacheEntry(long notificationId, ReleasePO release) {
            this.notificationId = notificationId;
            this.release = release;
        }

        public long getNotificationId() {
            return notificationId;
        }

        public ReleasePO getRelease() {
            return release;
        }
    }
}
