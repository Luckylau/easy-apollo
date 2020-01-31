package lucky.apollo.client.foundation.spi;

/**
 * @Author luckylau
 * @Date 2019/12/17
 */
public interface NetworkProvider extends Provider {
    /**
     * @return the host address, i.e. ip
     */
    String getHostAddress();

    /**
     * @return the host name
     */
    String getHostName();
}
