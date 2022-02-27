package lucky.apollo.client.config.impl.configFile;

import lucky.apollo.client.config.ConfigRepository;
import lucky.apollo.common.constant.ConfigFileFormat;

/**
 * @Author luckylau
 * @Date 2022/2/27
 */
public class YamlConfigFile extends PlainTextConfigFile {
    public YamlConfigFile(String namespace, ConfigRepository configRepository) {
        super(namespace, configRepository);
    }

    @Override
    public ConfigFileFormat getConfigFileFormat() {
        return ConfigFileFormat.YAML;
    }
}
