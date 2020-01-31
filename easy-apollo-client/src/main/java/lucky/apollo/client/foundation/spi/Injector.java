package lucky.apollo.client.foundation.spi;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
public interface Injector {
    /**
     * Returns the appropriate instance for the given injection type
     */
    <T> T getInstance(Class<T> clazz);

    /**
     * Returns the appropriate instance for the given injection type and name
     */
    <T> T getInstance(Class<T> clazz, String name);
}
