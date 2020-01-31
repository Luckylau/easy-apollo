package lucky.apollo.client.foundation.spi;

/**
 * @Author liuJun
 * @Date 2019/12/17
 */
public interface ProviderManager {
    String getProperty(String name, String defaultValue);

    <T extends Provider> T provider(Class<T> clazz);
}
