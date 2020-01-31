package lucky.apollo.client.foundation.spi.impl;

import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.foundation.spi.Provider;
import lucky.apollo.client.foundation.spi.ProviderManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author liuJun
 * @Date 2019/12/17
 */
@Slf4j
public class DefaultProviderManager implements ProviderManager {

    private Map<Class<? extends Provider>, Provider> m_providers = new LinkedHashMap<>();

    public DefaultProviderManager() {
        // Load per-application configuration, like app id, from classpath://META-INF/app.properties
        Provider applicationProvider = new DefaultApplicationProvider();
        applicationProvider.initialize();
        register(applicationProvider);

        // Load network parameters
        Provider networkProvider = new DefaultNetworkProvider();
        networkProvider.initialize();
        register(networkProvider);

        // Load environment (fat, fws, uat, prod ...) and dc, from /opt/settings/server.properties, JVM property and/or OS
        // environment variables.
        Provider serverProvider = new DefaultServerProvider();
        serverProvider.initialize();
        register(serverProvider);
    }

    public synchronized void register(Provider provider) {
        m_providers.put(provider.getType(), provider);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        return null;
    }

    @Override
    public <T extends Provider> T provider(Class<T> clazz) {
        return null;
    }
}
