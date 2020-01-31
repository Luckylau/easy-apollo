package lucky.apollo.client.config;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
public interface ConfigRegistry {
    /**
     * Register the config factory for the namespace specified.
     *
     * @param namespace the namespace
     * @param factory   the factory for this namespace
     */
    public void register(String namespace, ConfigFactory factory);

    /**
     * Get the registered config factory for the namespace.
     *
     * @param namespace the namespace
     * @return the factory registered for this namespace
     */
    public ConfigFactory getFactory(String namespace);
}
