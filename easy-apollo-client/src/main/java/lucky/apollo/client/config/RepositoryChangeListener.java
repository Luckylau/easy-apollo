package lucky.apollo.client.config;

import java.util.Properties;

/**
 * @Author luckylau
 * @Date 2020/9/19
 */
public interface RepositoryChangeListener {

    /**
     * Invoked when config repository changes.
     *
     * @param namespace     the namespace of this repository change
     * @param newProperties the properties after change
     */
    void onRepositoryChange(String namespace, Properties newProperties);
}
