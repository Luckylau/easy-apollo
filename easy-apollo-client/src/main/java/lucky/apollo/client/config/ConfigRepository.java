package lucky.apollo.client.config;

import lucky.apollo.client.constant.ConfigSourceType;

import java.util.Properties;

/**
 * @Author luckylau
 * @Date 2020/9/20
 */
public interface ConfigRepository {
    /**
     * Get the config from this repository.
     *
     * @return config
     */
    Properties getConfig();

    /**
     * Set the fallback repo for this repository.
     *
     * @param upstreamConfigRepository the upstream repo
     */
    void setUpstreamRepository(ConfigRepository upstreamConfigRepository);

    /**
     * Add change listener.
     *
     * @param listener the listener to observe the changes
     */
    void addChangeListener(RepositoryChangeListener listener);

    /**
     * Remove change listener.
     *
     * @param listener the listener to remove
     */
    void removeChangeListener(RepositoryChangeListener listener);

    /**
     * Return the config's source type, i.e. where is the config loaded from
     *
     * @return the config's source type
     */
    ConfigSourceType getSourceType();
}
