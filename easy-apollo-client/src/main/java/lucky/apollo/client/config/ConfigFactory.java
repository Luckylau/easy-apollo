package lucky.apollo.client.config;

import lucky.apollo.common.constant.ConfigFileFormat;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
public interface ConfigFactory {
    /**
     * Create the config instance for the namespace.
     *
     * @param namespace the namespace
     * @return the newly created config instance
     */
    Config create(String namespace);

    /**
     * Create the config file instance for the namespace
     *
     * @param configFileFormat
     * @param namespace        the namespace
     * @return the newly created config file instance
     */
    ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat);
}
