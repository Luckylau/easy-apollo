package lucky.apollo.configservice.cache;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.utils.ApolloThreadFactory;
import lucky.apollo.core.config.ServiceConfig;
import lucky.apollo.core.entity.ReleaseMessagePO;
import lucky.apollo.core.message.MessageTopic;
import lucky.apollo.core.message.ReleaseMessageListener;
import lucky.apollo.core.repository.ReleaseMessageRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author luckylau
 * @Date 2019/12/9
 */
@Component
@Slf4j
public class ReleaseMessageServiceWithCache implements ReleaseMessageListener, InitializingBean {

    private final ReleaseMessageRepository releaseMessageRepository;

    private final ServiceConfig serviceConfig;

    private int scanInterval;
    private TimeUnit scanIntervalTimeUnit;

    private volatile long maxIdScanned;

    private ConcurrentMap<String, ReleaseMessagePO> releaseMessageCache;

    private AtomicBoolean doScan;
    private ExecutorService executorService;

    public ReleaseMessageServiceWithCache(
            final ReleaseMessageRepository releaseMessageRepository,
            final ServiceConfig serviceConfig) {
        this.releaseMessageRepository = releaseMessageRepository;
        this.serviceConfig = serviceConfig;
        initialize();
    }

    private void initialize() {
        releaseMessageCache = Maps.newConcurrentMap();
        doScan = new AtomicBoolean(true);
        executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), ApolloThreadFactory.create("ReleaseMessageServiceWithCache", true));

    }

    public List<ReleaseMessagePO> findLatestReleaseMessagesGroupByMessages(Set<String> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }
        List<ReleaseMessagePO> releaseMessages = Lists.newArrayList();

        for (String message : messages) {
            ReleaseMessagePO releaseMessage = releaseMessageCache.get(message);
            if (releaseMessage != null) {
                releaseMessages.add(releaseMessage);
            }
        }

        return releaseMessages;
    }


    @Override
    public void handleMessage(ReleaseMessagePO message, String channel) {
        //Could stop once the ReleaseMessageScanner starts to work
        doScan.set(false);
        log.info("message received - channel: {}, message: {}", channel, message);

        String content = message.getMessage();
        log.info("Apollo.ReleaseMessageService.UpdateCache", String.valueOf(message.getId()));
        if (!MessageTopic.APOLLO_RELEASE_TOPIC.equals(channel) || Strings.isNullOrEmpty(content)) {
            return;
        }

        long gap = message.getId() - maxIdScanned;
        if (gap == 1) {
            mergeReleaseMessage(message);
        } else if (gap > 1) {
            //gap found!
            loadReleaseMessages(maxIdScanned);
        }
    }

    private synchronized void mergeReleaseMessage(ReleaseMessagePO releaseMessage) {
        ReleaseMessagePO old = releaseMessageCache.get(releaseMessage.getMessage());
        if (old == null || releaseMessage.getId() > old.getId()) {
            releaseMessageCache.put(releaseMessage.getMessage(), releaseMessage);
            maxIdScanned = releaseMessage.getId();
        }
    }

    private void loadReleaseMessages(long startId) {
        boolean hasMore = true;
        while (hasMore && !Thread.currentThread().isInterrupted()) {
            //current batch is 500
            List<ReleaseMessagePO> releaseMessages = releaseMessageRepository
                    .findFirst500ByIdGreaterThanOrderByIdAsc(startId);
            if (CollectionUtils.isEmpty(releaseMessages)) {
                break;
            }
            releaseMessages.forEach(this::mergeReleaseMessage);
            int scanned = releaseMessages.size();
            startId = releaseMessages.get(scanned - 1).getId();
            hasMore = scanned == 500;
            log.info("Loaded {} release messages with startId {}", scanned, startId);
        }
    }

    private void populateDataBaseInterval() {
        scanInterval = serviceConfig.releaseMessageCacheScanInterval();
        scanIntervalTimeUnit = serviceConfig.releaseMessageCacheScanIntervalTimeUnit();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        populateDataBaseInterval();
        //block the startup process until load finished
        //this should happen before ReleaseMessageScanner due to autowire
        loadReleaseMessages(0);

        executorService.submit(() -> {
            while (doScan.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    loadReleaseMessages(maxIdScanned);
                } catch (Throwable ex) {
                    log.error("Scan new release messages failed", ex);
                }
                try {
                    scanIntervalTimeUnit.sleep(scanInterval);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        });
    }
}
