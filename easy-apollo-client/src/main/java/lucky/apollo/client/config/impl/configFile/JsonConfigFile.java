package lucky.apollo.client.config.impl.configFile;

import lucky.apollo.client.config.ConfigRepository;
import lucky.apollo.common.constant.ConfigFileFormat;

/**
 * @Author luckylau
 * @Date 2022/2/27
 */
public class JsonConfigFile extends PlainTextConfigFile {
    public JsonConfigFile(String namespace,
                          ConfigRepository configRepository) {
        super(namespace, configRepository);
    }

    @Override
    public ConfigFileFormat getConfigFileFormat() {
        return ConfigFileFormat.JSON;
    }
}
