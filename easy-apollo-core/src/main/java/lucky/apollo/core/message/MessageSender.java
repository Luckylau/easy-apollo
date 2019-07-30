package lucky.apollo.core.message;

import com.google.common.collect.Queues;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.utils.ApolloThreadFactory;
import lucky.apollo.core.entity.ReleaseMessagePO;
import lucky.apollo.core.repository.ReleaseMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@Component
@Slf4j
public class MessageSender {
    private static final int CLEAN_QUEUE_MAX_SIZE = 100;
    private final ExecutorService cleanExecutorService;
    private final AtomicBoolean cleanStopped;
    private BlockingQueue<Long> toClean = Queues.newLinkedBlockingQueue(CLEAN_QUEUE_MAX_SIZE);
    @Autowired
    private ReleaseMessageRepository releaseMessageRepository;

    public MessageSender() {
        cleanExecutorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), ApolloThreadFactory.create("DatabaseMessageSender", true));
        cleanStopped = new AtomicBoolean(false);
    }

    @Transactional
    public void sendMessage(String message, String channel) {
        log.info("Sending message {} to channel {}", message, channel);
        if (!Objects.equals(channel, MessageTopic.APOLLO_RELEASE_TOPIC)) {
            log.warn("Channel {} not supported by DatabaseMessageSender!");
            return;
        }

        try {
            ReleaseMessagePO newMessage = releaseMessageRepository.save(new ReleaseMessagePO(message));
            toClean.offer(newMessage.getId());
        } catch (Throwable ex) {
            log.error("Sending message to database failed", ex);
            throw ex;
        }
    }

    @PostConstruct
    private void initialize() {
        cleanExecutorService.submit(() -> {
            while (!cleanStopped.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    Long rm = toClean.poll(1, TimeUnit.SECONDS);
                    if (rm != null) {
                        cleanMessage(rm);
                    } else {
                        TimeUnit.SECONDS.sleep(5);
                    }
                } catch (Throwable ex) {
                    log.error("clean Executor Service error ", ex);
                }
            }
        });
    }

    private void cleanMessage(Long id) {
        boolean hasMore = true;
        //double check in case the release message is rolled back
        ReleaseMessagePO releaseMessage = releaseMessageRepository.findById(id).orElse(null);
        if (releaseMessage == null) {
            return;
        }
        while (hasMore && !Thread.currentThread().isInterrupted()) {
            List<ReleaseMessagePO> messages = releaseMessageRepository.findFirst100ByMessageAndIdLessThanOrderByIdAsc(
                    releaseMessage.getMessage(), releaseMessage.getId());

            releaseMessageRepository.deleteAll(messages);
            hasMore = messages.size() == 100;
        }
    }

    private void stopClean() {
        cleanStopped.set(true);
    }
}