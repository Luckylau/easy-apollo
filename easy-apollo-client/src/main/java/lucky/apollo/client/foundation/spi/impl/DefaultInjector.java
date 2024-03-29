package lucky.apollo.client.foundation.spi.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import lucky.apollo.client.config.*;
import lucky.apollo.client.config.impl.DefaultConfigFactory;
import lucky.apollo.client.config.impl.DefaultConfigFactoryManager;
import lucky.apollo.client.config.impl.DefaultConfigManager;
import lucky.apollo.client.config.impl.DefaultConfigRegistry;
import lucky.apollo.client.exception.ApolloConfigException;
import lucky.apollo.client.foundation.spi.Injector;
import lucky.apollo.client.service.RemoteConfigLongPollService;
import lucky.apollo.client.util.ConfigUtil;
import lucky.apollo.client.util.http.HttpUtil;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
public class DefaultInjector implements Injector {

    private com.google.inject.Injector m_injector;

    public DefaultInjector() {
        try {
            m_injector = Guice.createInjector(new ApolloModule());
        } catch (Throwable ex) {
            throw new ApolloConfigException("Unable to initialize Guice Injector!", ex);
        }
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        try {
            return m_injector.getInstance(clazz);
        } catch (Throwable ex) {
            throw new ApolloConfigException(
                    String.format("Unable to load instance for %s!", clazz.getName()), ex);
        }
    }

    @Override
    public <T> T getInstance(Class<T> clazz, String name) {
        return null;
    }

    private static class ApolloModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ConfigManager.class).to(DefaultConfigManager.class).in(Singleton.class);
            bind(ConfigFactoryManager.class).to(DefaultConfigFactoryManager.class).in(Singleton.class);
            bind(ConfigRegistry.class).to(DefaultConfigRegistry.class).in(Singleton.class);
            bind(ConfigFactory.class).to(DefaultConfigFactory.class).in(Singleton.class);
            bind(ConfigUtil.class).in(Singleton.class);
            bind(HttpUtil.class).in(Singleton.class);
            bind(ConfigServiceLocator.class).in(Singleton.class);
            bind(RemoteConfigLongPollService.class).in(Singleton.class);
        }
    }
}
