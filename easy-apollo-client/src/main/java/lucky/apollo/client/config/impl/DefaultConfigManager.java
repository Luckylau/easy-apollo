package lucky.apollo.client.config.impl;

import lucky.apollo.client.config.Config;
import lucky.apollo.client.config.ConfigFile;
import lucky.apollo.client.config.ConfigManager;
import lucky.apollo.common.constant.ConfigFileFormat;

/**
 * @Author liuJun
 * @Date 2019/12/13
 */
public class DefaultConfigManager implements ConfigManager {
    @Override
    public Config getConfig(String namespace) {
        return null;
    }

    @Override
    public ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat) {
        return null;
    }
}
