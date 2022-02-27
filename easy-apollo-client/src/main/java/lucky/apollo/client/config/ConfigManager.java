package lucky.apollo.client.config;

import lucky.apollo.common.constant.ConfigFileFormat;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
public interface ConfigManager {
    /**
     * Get the config instance for the namespace specified.
     *
     * @param namespace the namespace
     * @return the config instance for the namespace
     */
    Config getConfig(String namespace);

    /**
     * Get the config file instance for the namespace specified.
     *
     * @param namespace        the namespace
     * @param configFileFormat the config file format
     * @return the config file instance for the namespace
     */
    ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat);
}
