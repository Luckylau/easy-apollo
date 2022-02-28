package lucky.apollo.client.config.impl;

import com.google.common.collect.Maps;
import lucky.apollo.client.build.ApolloInjector;
import lucky.apollo.client.config.*;
import lucky.apollo.common.constant.ConfigFileFormat;

import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
public class DefaultConfigManager implements ConfigManager {
    private ConfigFactoryManager m_factoryManager;

    private Map<String, Config> m_configs = Maps.newConcurrentMap();
    private Map<String, ConfigFile> m_configFiles = Maps.newConcurrentMap();

    public DefaultConfigManager() {
        m_factoryManager = ApolloInjector.getInstance(ConfigFactoryManager.class);
    }

    @Override
    public Config getConfig(String namespace) {
        Config config = m_configs.get(namespace);

        if (config == null) {
            synchronized (this) {
                config = m_configs.get(namespace);

                if (config == null) {
                    ConfigFactory factory = m_factoryManager.getFactory(namespace);

                    config = factory.create(namespace);
                    m_configs.put(namespace, config);
                }
            }
        }

        return config;
    }

    @Override
    public ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat) {
        String namespaceFileName = String.format("%s.%s", namespace, configFileFormat.getValue());
        ConfigFile configFile = m_configFiles.get(namespaceFileName);

        if (configFile == null) {
            synchronized (this) {
                configFile = m_configFiles.get(namespaceFileName);

                if (configFile == null) {
                    ConfigFactory factory = m_factoryManager.getFactory(namespaceFileName);

                    configFile = factory.createConfigFile(namespaceFileName, configFileFormat);
                    m_configFiles.put(namespaceFileName, configFile);
                }
            }
        }

        return configFile;
    }
}
