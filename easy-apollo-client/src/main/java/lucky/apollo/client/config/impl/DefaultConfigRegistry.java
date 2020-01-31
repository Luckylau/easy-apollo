package lucky.apollo.client.config.impl;

import lucky.apollo.client.config.ConfigFactory;
import lucky.apollo.client.config.ConfigRegistry;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
public class DefaultConfigRegistry implements ConfigRegistry {
    @Override
    public void register(String namespace, ConfigFactory factory) {

    }

    @Override
    public ConfigFactory getFactory(String namespace) {
        return null;
    }
}
