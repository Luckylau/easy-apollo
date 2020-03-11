package lucky.apollo.client.build;

import lucky.apollo.client.exception.ApolloConfigException;
import lucky.apollo.client.foundation.ServiceBootstrap;
import lucky.apollo.client.foundation.spi.Injector;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
public class ApolloInjector {
    private static volatile Injector s_injector;
    private static final Object lock = new Object();

    private static Injector getInjector() {
        if (s_injector == null) {
            synchronized (lock) {
                if (s_injector == null) {
                    try {
                        s_injector = ServiceBootstrap.loadFirst(Injector.class);
                    } catch (Throwable ex) {
                        throw new ApolloConfigException("Unable to initialize Apollo Injector!", ex);

                    }
                }
            }
        }

        return s_injector;
    }

    public static <T> T getInstance(Class<T> clazz) {
        try {
            return getInjector().getInstance(clazz);
        } catch (Throwable ex) {
            throw new ApolloConfigException(String.format("Unable to load instance for type %s!", clazz.getName()), ex);
        }
    }

    public static <T> T getInstance(Class<T> clazz, String name) {
        try {
            return getInjector().getInstance(clazz, name);
        } catch (Throwable ex) {
            throw new ApolloConfigException(
                    String.format("Unable to load instance for type %s and name %s !", clazz.getName(), name), ex);
        }
    }
}
