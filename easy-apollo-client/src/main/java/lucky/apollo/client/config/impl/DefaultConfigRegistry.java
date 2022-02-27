package lucky.apollo.client.config.impl;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.config.ConfigFactory;
import lucky.apollo.client.config.ConfigRegistry;

import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
@Slf4j
public class DefaultConfigRegistry implements ConfigRegistry {
    private Map<String, ConfigFactory> m_instances = Maps.newConcurrentMap();

    @Override
    public void register(String namespace, ConfigFactory factory) {
        if (m_instances.containsKey(namespace)) {
            log.warn("ConfigFactory({}) is overridden by {}!", namespace, factory.getClass());
        }
        m_instances.put(namespace, factory);
    }

    @Override
    public ConfigFactory getFactory(String namespace) {
        return m_instances.get(namespace);
    }
}
