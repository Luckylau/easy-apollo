package lucky.apollo.client.config;

import lucky.apollo.client.constant.ConfigSourceType;
import lucky.apollo.client.listener.ConfigFileChangeListener;
import lucky.apollo.common.constant.ConfigFileFormat;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
public interface ConfigFile {
    /**
     * Get file content of the namespace
     *
     * @return file content, {@code null} if there is no content
     */
    String getContent();

    /**
     * Whether the config file has any content
     *
     * @return true if it has content, false otherwise.
     */
    boolean hasContent();

    /**
     * Get the namespace of this config file instance
     *
     * @return the namespace
     */
    String getNamespace();

    /**
     * Get the file format of this config file instance
     *
     * @return the config file format enum
     */
    ConfigFileFormat getConfigFileFormat();

    /**
     * Add change listener to this config file instance.
     *
     * @param listener the config file change listener
     */
    void addChangeListener(ConfigFileChangeListener listener);

    /**
     * Remove the change listener
     *
     * @param listener the specific config change listener to remove
     * @return true if the specific config change listener is found and removed
     */
    public boolean removeChangeListener(ConfigChangeListener listener);

    /**
     * Return the config's source type, i.e. where is the config loaded from
     *
     * @return the config's source type
     */
    public ConfigSourceType getSourceType();
}
