package lucky.apollo.client.foundation.spi;

import java.io.InputStream;

/**
 * @Author liuJun
 * @Date 2019/12/17
 */
public interface ApplicationProvider extends Provider {
    /**
     * @return the application's app id
     */
    String getAppId();

    /**
     * @return whether the application's app id is set or not
     */
    boolean isAppIdSet();

    /**
     * Initialize the application provider with the specified input stream
     */
    void initialize(InputStream in);
}
