package lucky.apollo.client.config.impl;

import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.build.ApolloInjector;
import lucky.apollo.client.config.*;
import lucky.apollo.client.config.impl.configFile.*;
import lucky.apollo.client.util.ConfigUtil;
import lucky.apollo.common.constant.ConfigFileFormat;

/**
 * @Author luckylau
 * @Date 2022/2/27
 */
@Slf4j
public class DefaultConfigFactory implements ConfigFactory {

    private ConfigUtil m_configUtil;

    public DefaultConfigFactory() {
        m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    }

    @Override
    public Config create(String namespace) {
        return new DefaultConfig(namespace, createLocalConfigRepository(namespace));
    }

    @Override
    public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
        ConfigRepository configRepository= createLocalConfigRepository(namespace);
        switch (configFileFormat) {
            case Properties:
                return new PropertiesConfigFile(namespace, configRepository);
            case XML:
                return new XmlConfigFile(namespace, configRepository);
            case JSON:
                return new JsonConfigFile(namespace, configRepository);
            case YAML:
                return new YamlConfigFile(namespace, configRepository);
            case YML:
                return new YmlConfigFile(namespace, configRepository);
        }
        return null;
    }

    LocalFileConfigRepository createLocalConfigRepository(String namespace) {
        return new LocalFileConfigRepository(namespace, new RemoteConfigRepository(namespace));
    }
}
