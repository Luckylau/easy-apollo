package lucky.apollo.common.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
public class ApolloThreadFactory implements ThreadFactory {
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("EasyApollo");
    private final AtomicLong threadNumber = new AtomicLong(1);
    private final String namePrefix;
    private final Boolean daemon;

    private ApolloThreadFactory(String namePrefix, Boolean daemon) {
        this.daemon = daemon;
        this.namePrefix = namePrefix;
    }

    public static ThreadFactory create(String namePrefix, boolean daemon) {
        return new ApolloThreadFactory(namePrefix, daemon);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(THREAD_GROUP, r, THREAD_GROUP.getName() + "-" + namePrefix + "-" + threadNumber.getAndIncrement());
        thread.setDaemon(daemon);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}