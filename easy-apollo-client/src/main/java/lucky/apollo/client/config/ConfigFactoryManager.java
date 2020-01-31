package lucky.apollo.client.config;

/**
 * @Author liuJun
 * @Date 2019/12/13
 */
public interface ConfigFactoryManager {
    /**
     * Get the config factory for the namespace.
     *
     * @param namespace the namespace
     * @return the config factory for this namespace
     */
    ConfigFactory getFactory(String namespace);
}
