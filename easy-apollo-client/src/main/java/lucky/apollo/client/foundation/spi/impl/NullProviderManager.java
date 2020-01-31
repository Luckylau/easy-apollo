package lucky.apollo.client.foundation.spi.impl;

import lucky.apollo.client.foundation.spi.ProviderManager;

/**
 * @Author liuJun
 * @Date 2019/12/17
 */
public class NullProviderManager implements ProviderManager {
    public static final NullProvider provider = new NullProvider();

    @Override
    public String getProperty(String name, String defaultValue) {
        return defaultValue;
    }

    @Override
    public NullProvider provider(Class clazz) {
        return provider;
    }

    @Override
    public String toString() {
        return provider.toString();
    }
}
