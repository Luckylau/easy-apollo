package lucky.apollo.client.foundation;

import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.foundation.spi.ApplicationProvider;
import lucky.apollo.client.foundation.spi.NetworkProvider;
import lucky.apollo.client.foundation.spi.ProviderManager;
import lucky.apollo.client.foundation.spi.ServerProvider;
import lucky.apollo.client.foundation.spi.impl.NullProviderManager;

/**
 * @Author luckylau
 * @Date 2019/12/17
 */
@Slf4j
public abstract class Foundation {
    private static Object lock = new Object();

    private static volatile ProviderManager s_manager;

    // Encourage early initialization and fail early if it happens.
    static {
        getManager();
    }

    private static ProviderManager getManager() {
        try {
            if (s_manager == null) {
                // Double locking to make sure only one thread initializes ProviderManager.
                synchronized (lock) {
                    if (s_manager == null) {
                        s_manager = ServiceBootstrap.loadFirst(ProviderManager.class);
                    }
                }
            }

            return s_manager;
        } catch (Throwable ex) {
            s_manager = new NullProviderManager();
            log.error("Initialize ProviderManager failed.", ex);
            return s_manager;
        }
    }

    public static ApplicationProvider app() {
        try {
            return getManager().provider(ApplicationProvider.class);
        } catch (Exception ex) {
            log.error("Initialize ApplicationProvider failed.", ex);
            return NullProviderManager.provider;
        }
    }

    public static NetworkProvider net() {
        try {
            return getManager().provider(NetworkProvider.class);
        } catch (Exception ex) {
            log.error("Initialize NetworkProvider failed.", ex);
            return NullProviderManager.provider;
        }
    }

    public static ServerProvider server() {
        try {
            return getManager().provider(ServerProvider.class);
        } catch (Exception ex) {
            log.error("Initialize ServerProvider failed.", ex);
            return NullProviderManager.provider;
        }
    }
}
