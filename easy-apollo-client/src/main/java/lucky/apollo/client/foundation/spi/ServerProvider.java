package lucky.apollo.client.foundation.spi;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author liuJun
 * @Date 2019/12/17
 */
public interface ServerProvider extends Provider {
    /**
     * @return current environment or {@code null} if not set
     */
    String getEnvType();

    /**
     * @return whether current environment is set or not
     */
    boolean isEnvTypeSet();

    /**
     * @return current data center or {@code null} if not set
     */
    String getDataCenter();

    /**
     * @return whether data center is set or not
     */
    boolean isDataCenterSet();

    /**
     * Initialize server provider with the specified input stream
     *
     * @throws IOException
     */
    void initialize(InputStream in) throws IOException;
}
