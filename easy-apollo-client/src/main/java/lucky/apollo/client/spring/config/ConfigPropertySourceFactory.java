package lucky.apollo.client.spring.config;

import com.google.common.collect.Lists;
import lucky.apollo.client.config.Config;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2021/2/21
 */
public class ConfigPropertySourceFactory {
    private final List<ConfigPropertySource> configPropertySources = Lists.newLinkedList();

    public ConfigPropertySource getConfigPropertySource(String name, Config source) {
        ConfigPropertySource configPropertySource = new ConfigPropertySource(name, source);

        configPropertySources.add(configPropertySource);

        return configPropertySource;
    }

    public List<ConfigPropertySource> getAllConfigPropertySources() {
        return Lists.newLinkedList(configPropertySources);
    }
}
