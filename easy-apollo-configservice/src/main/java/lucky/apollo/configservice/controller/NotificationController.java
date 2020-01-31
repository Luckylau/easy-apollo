package lucky.apollo.configservice.controller;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.entity.dto.ApolloConfigNotificationDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.ApolloThreadFactory;
import lucky.apollo.configservice.cache.ReleaseMessageServiceWithCache;
import lucky.apollo.configservice.component.NamespaceService;
import lucky.apollo.configservice.utils.EntityManagerUtil;
import lucky.apollo.configservice.utils.WatchKeysUtil;
import lucky.apollo.configservice.wrapper.DeferredResultWrapper;
import lucky.apollo.core.config.ServiceConfig;
import lucky.apollo.core.entity.ReleaseMessagePO;
import lucky.apollo.core.message.MessageTopic;
import lucky.apollo.core.message.ReleaseMessageListener;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Author luckylau
 * @Date 2019/12/7
 */
@RestController
@RequestMapping("/notifications")
@Slf4j
public class NotificationController implements ReleaseMessageListener {

    private static final Splitter STRING_SPLITTER =
            Splitter.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR).omitEmptyStrings();

    private final Multimap<String, DeferredResultWrapper> deferredResults =
            Multimaps.synchronizedSetMultimap(HashMultimap.create());

    private static final Type notificationsTypeReference =
            new TypeToken<List<ApolloConfigNotificationDTO>>() {
            }.getType();

    private final ExecutorService largeNotificationBatchExecutorService;

    private final WatchKeysUtil watchKeysUtil;
    private final ReleaseMessageServiceWithCache releaseMessageService;
    private final EntityManagerUtil entityManagerUtil;
    private final NamespaceService namespaceService;
    private final Gson gson;
    private final ServiceConfig serviceConfig;

    public NotificationController(
            final WatchKeysUtil watchKeysUtil,
            final ReleaseMessageServiceWithCache releaseMessageService,
            final EntityManagerUtil entityManagerUtil,
            final NamespaceService namespaceService,
            final Gson gson,
            final ServiceConfig serviceConfig) {
        largeNotificationBatchExecutorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), ApolloThreadFactory.create("NotificationController", true));
        this.watchKeysUtil = watchKeysUtil;
        this.releaseMessageService = releaseMessageService;
        this.entityManagerUtil = entityManagerUtil;
        this.namespaceService = namespaceService;
        this.gson = gson;
        this.serviceConfig = serviceConfig;
    }

    @GetMapping
    public DeferredResult<ResponseEntity<List<ApolloConfigNotificationDTO>>> pollNotification(
            @RequestParam(value = "appId") String appId,
            @RequestParam(value = "cluster") String cluster,
            @RequestParam(value = "notifications") String notificationsAsString,
            @RequestParam(value = "dataCenter", required = false) String dataCenter,
            @RequestParam(value = "ip", required = false) String clientIp) {
        List<ApolloConfigNotificationDTO> notifications = null;

        try {
            notifications =
                    gson.fromJson(notificationsAsString, notificationsTypeReference);
        } catch (Throwable ex) {
            throw new BadRequestException("Invalid format of notifications: " + notificationsAsString);
        }


        DeferredResultWrapper deferredResultWrapper = new DeferredResultWrapper();
        Set<String> namespaces = Sets.newHashSet();
        Map<String, Long> clientSideNotifications = Maps.newHashMap();
        Map<String, ApolloConfigNotificationDTO> filteredNotifications = filterNotifications(appId, notifications);

        for (Map.Entry<String, ApolloConfigNotificationDTO> notificationEntry : filteredNotifications.entrySet()) {
            String normalizedNamespace = notificationEntry.getKey();
            ApolloConfigNotificationDTO notification = notificationEntry.getValue();
            namespaces.add(normalizedNamespace);
            clientSideNotifications.put(normalizedNamespace, notification.getNotificationId());
            if (!Objects.equals(notification.getNamespaceName(), normalizedNamespace)) {
                deferredResultWrapper.recordNamespaceNameNormalizedResult(notification.getNamespaceName(), normalizedNamespace);
            }
        }

        if (CollectionUtils.isEmpty(namespaces)) {
            throw new BadRequestException("Invalid format of notifications: " + notificationsAsString);
        }

        Multimap<String, String> watchedKeysMap =
                watchKeysUtil.assembleAllWatchKeys(appId, cluster, namespaces, dataCenter);

        Set<String> watchedKeys = Sets.newHashSet(watchedKeysMap.values());

        /**
         * 1、set deferredResult before the check, for avoid more waiting
         * If the check before setting deferredResult,it may receive a notification the next time
         * when method handleMessage is executed between check and set deferredResult.
         */
        deferredResultWrapper
                .onTimeout(() -> logWatchedKeys(watchedKeys, "Apollo.LongPoll.TimeOutKeys"));

        deferredResultWrapper.onCompletion(() -> {
            //unregister all keys
            for (String key : watchedKeys) {
                deferredResults.remove(key, deferredResultWrapper);
            }
            logWatchedKeys(watchedKeys, "Apollo.LongPoll.CompletedKeys");
        });

        //register all keys
        for (String key : watchedKeys) {
            this.deferredResults.put(key, deferredResultWrapper);
        }

        logWatchedKeys(watchedKeys, "Apollo.LongPoll.RegisteredKeys");
        log.debug("Listening {} from appId: {}, cluster: {}, namespace: {}, datacenter: {}",
                watchedKeys, appId, cluster, namespaces, dataCenter);

        /**
         * 2、check new release
         */
        List<ReleaseMessagePO> latestReleaseMessages =
                releaseMessageService.findLatestReleaseMessagesGroupByMessages(watchedKeys);

        /**
         * Manually close the entity manager.
         * Since for async request, Spring won't do so until the request is finished,
         * which is unacceptable since we are doing long polling - means the db connection would be hold
         * for a very long time
         */
        entityManagerUtil.closeEntityManager();

        List<ApolloConfigNotificationDTO> newNotifications =
                getApolloConfigNotifications(namespaces, clientSideNotifications, watchedKeysMap,
                        latestReleaseMessages);

        if (!CollectionUtils.isEmpty(newNotifications)) {
            deferredResultWrapper.setResult(newNotifications);
        }

        return deferredResultWrapper.getResult();
    }

    private List<ApolloConfigNotificationDTO> getApolloConfigNotifications(Set<String> namespaces,
                                                                           Map<String, Long> clientSideNotifications,
                                                                           Multimap<String, String> watchedKeysMap,
                                                                           List<ReleaseMessagePO> latestReleaseMessages) {
        List<ApolloConfigNotificationDTO> newNotifications = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(latestReleaseMessages)) {
            Map<String, Long> latestNotifications = Maps.newHashMap();
            for (ReleaseMessagePO releaseMessage : latestReleaseMessages) {
                latestNotifications.put(releaseMessage.getMessage(), releaseMessage.getId());
            }

            for (String namespace : namespaces) {
                long clientSideId = clientSideNotifications.get(namespace);
                long latestId = ConfigConsts.NOTIFICATION_ID_PLACEHOLDER;
                Collection<String> namespaceWatchedKeys = watchedKeysMap.get(namespace);
                for (String namespaceWatchedKey : namespaceWatchedKeys) {
                    long namespaceNotificationId =
                            latestNotifications.getOrDefault(namespaceWatchedKey, ConfigConsts.NOTIFICATION_ID_PLACEHOLDER);
                    if (namespaceNotificationId > latestId) {
                        latestId = namespaceNotificationId;
                    }
                }
                if (latestId > clientSideId) {
                    ApolloConfigNotificationDTO notification = new ApolloConfigNotificationDTO(namespace, latestId);
                    namespaceWatchedKeys.stream().filter(latestNotifications::containsKey).forEach(namespaceWatchedKey ->
                            notification.addMessage(namespaceWatchedKey, latestNotifications.get(namespaceWatchedKey)));
                    newNotifications.add(notification);
                }
            }
        }
        return newNotifications;
    }


    @Override
    public void handleMessage(ReleaseMessagePO message, String channel) {
        log.info("message received - channel: {}, message: {}", channel, message);

        String content = message.getMessage();
        if (!MessageTopic.APOLLO_RELEASE_TOPIC.equals(channel) || Strings.isNullOrEmpty(content)) {
            return;
        }

        String changedNamespace = retrieveNamespaceFromReleaseMessage.apply(content);

        if (Strings.isNullOrEmpty(changedNamespace)) {
            log.error("message format invalid - {}", content);
            return;
        }

        if (!deferredResults.containsKey(content)) {
            return;
        }

        //create a new list to avoid ConcurrentModificationException
        List<DeferredResultWrapper> results = Lists.newArrayList(deferredResults.get(content));

        ApolloConfigNotificationDTO configNotification = new ApolloConfigNotificationDTO(changedNamespace, message.getId());
        configNotification.addMessage(content, message.getId());

        //do async notification if too many clients
        if (results.size() > serviceConfig.releaseMessageNotificationBatch()) {
            largeNotificationBatchExecutorService.submit(() -> {
                log.debug("Async notify {} clients for key {} with batch {}", results.size(), content,
                        serviceConfig.releaseMessageNotificationBatch());
                for (int i = 0; i < results.size(); i++) {
                    if (i > 0 && i % serviceConfig.releaseMessageNotificationBatch() == 0) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(serviceConfig.releaseMessageNotificationBatchIntervalInMilli());
                        } catch (InterruptedException e) {
                            //ignore
                        }
                    }
                    log.debug("Async notify {}", results.get(i));
                    results.get(i).setResult(configNotification);
                }
            });
            return;
        }

        log.debug("Notify {} clients for key {}", results.size(), content);

        for (DeferredResultWrapper result : results) {
            result.setResult(configNotification);
        }
        log.debug("Notification completed");
    }

    private Map<String, ApolloConfigNotificationDTO> filterNotifications(String appId,
                                                                         List<ApolloConfigNotificationDTO> notifications) {
        Map<String, ApolloConfigNotificationDTO> filteredNotifications = Maps.newHashMap();
        for (ApolloConfigNotificationDTO notification : notifications) {
            if (Strings.isNullOrEmpty(notification.getNamespaceName())) {
                continue;
            }
            //strip out .properties suffix
            String originalNamespace = namespaceService.filterNamespaceName(notification.getNamespaceName());
            notification.setNamespaceName(originalNamespace);
            //fix the character case issue, such as FX.apollo <-> fx.apollo
            String normalizedNamespace = namespaceService.normalizeNamespace(appId, originalNamespace);

            // in case client side namespace name has character case issue and has difference notification ids
            // such as FX.apollo = 1 but fx.apollo = 2, we should let FX.apollo have the chance to update its notification id
            // which means we should record FX.apollo = 1 here and ignore fx.apollo = 2
            if (filteredNotifications.containsKey(normalizedNamespace) &&
                    filteredNotifications.get(normalizedNamespace).getNotificationId() < notification.getNotificationId()) {
                continue;
            }

            filteredNotifications.put(normalizedNamespace, notification);
        }
        return filteredNotifications;
    }

    private void logWatchedKeys(Set<String> watchedKeys, String eventName) {
        for (String watchedKey : watchedKeys) {
            log.info("eventName:{}, watchedKey:{}", eventName, watchedKey);
        }
    }

    private static final Function<String, String> retrieveNamespaceFromReleaseMessage =
            releaseMessage -> {
                if (Strings.isNullOrEmpty(releaseMessage)) {
                    return null;
                }
                List<String> keys = STRING_SPLITTER.splitToList(releaseMessage);
                //message should be appId+cluster+namespace
                if (keys.size() != 3) {
                    log.error("message format invalid - {}", releaseMessage);
                    return null;
                }
                return keys.get(2);
            };
}
