package lucky.apollo.portal.metaservice;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.utils.ApolloThreadFactory;
import lucky.apollo.common.utils.NetUtil;
import lucky.apollo.common.utils.ResourceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author luckylau
 * @Date 2019/9/17
 */
@Slf4j
public class MetaDomainConsts {
    private static volatile List<String> availableMetaServerAddressCache = new ArrayList<>();

    private static final String META_SERVICE_URL_PATH = "/services/meta";

    private static final long REFRESH_INTERVAL_IN_SECOND = 60;

    private static final String metaServerAddresses;

    private static final AtomicBoolean periodicRefreshStarted = new AtomicBoolean(false);

    static {
        Properties prop = new Properties();
        prop = ResourceUtils.readConfigFile("apollo-meta.properties", prop);
        metaServerAddresses = prop.getProperty("apollo.meta");
    }

    public static String getDomain() {
        if (availableMetaServerAddressCache.isEmpty()) {
            initMetaServerAddress();
        }
        Collections.shuffle(availableMetaServerAddressCache);
        return availableMetaServerAddressCache.get(0);
    }


    private static void initMetaServerAddress() {
        //在多线程情况下保证只有一次执行成功
        if (periodicRefreshStarted.compareAndSet(false, true)) {
            schedulePeriodicRefresh();
        }
        updateMetaServerAddresses();
    }

    private static void schedulePeriodicRefresh() {
        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(1, ApolloThreadFactory.create("MetaServiceLocator", true));

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                updateMetaServerAddresses();
            } catch (Throwable ex) {
                log.warn(String.format("Refreshing meta server address failed, will retry in %d seconds",
                        REFRESH_INTERVAL_IN_SECOND), ex);
            }
        }, REFRESH_INTERVAL_IN_SECOND, REFRESH_INTERVAL_IN_SECOND, TimeUnit.SECONDS);
    }

    private static void updateMetaServerAddresses() {
        if (metaServerAddresses.contains(",")) {
            List<String> metaServers = Lists.newArrayList(metaServerAddresses.split(","));
            for (String address : metaServers) {
                address = address.trim();
                if (!NetUtil.pingUrl(address + META_SERVICE_URL_PATH)) {
                    availableMetaServerAddressCache.add(address);
                    log.debug("available meta server address {} ", address);
                } else {
                    log.warn("meta server {} not available", address);
                }
            }
        } else {
            //如果只配置一个地址，即使不可用，也将其放入
            if (!NetUtil.pingUrl(metaServerAddresses + META_SERVICE_URL_PATH)) {
                log.warn("meta server {} not available", metaServerAddresses);
            }
            if (availableMetaServerAddressCache.isEmpty()) {
                availableMetaServerAddressCache.add(metaServerAddresses);
            }
        }
    }


}
