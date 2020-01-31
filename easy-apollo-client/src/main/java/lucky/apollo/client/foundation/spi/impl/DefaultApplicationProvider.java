package lucky.apollo.client.foundation.spi.impl;

import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.foundation.io.BOMInputStream;
import lucky.apollo.client.foundation.spi.ApplicationProvider;
import lucky.apollo.client.foundation.spi.Provider;
import lucky.apollo.client.util.Utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @Author luckylau
 * @Date 2019/12/17
 */
@Slf4j
public class DefaultApplicationProvider implements ApplicationProvider {

    public static final String APP_PROPERTIES_CLASSPATH = "/META-INF/app.properties";
    private Properties m_appProperties = new Properties();

    private String m_appId;


    @Override
    public String getAppId() {
        return null;
    }

    @Override
    public boolean isAppIdSet() {
        return false;
    }

    @Override
    public void initialize(InputStream in) {
        try {
            if (in != null) {
                try {
                    m_appProperties.load(new InputStreamReader(new BOMInputStream(in), StandardCharsets.UTF_8));
                } finally {
                    in.close();
                }
            }

            initAppId();
        } catch (Throwable ex) {
            log.error("Initialize DefaultApplicationProvider failed.", ex);
        }
    }

    private void initAppId() {
        // 1. Get app.id from System Property
        m_appId = System.getProperty("app.id");
        if (!Utils.isBlank(m_appId)) {
            m_appId = m_appId.trim();
            log.info("App ID is set to {} by app.id property from System Property", m_appId);
            return;
        }

        //2. Try to get app id from OS environment variable
        m_appId = System.getenv("APP_ID");
        if (!Utils.isBlank(m_appId)) {
            m_appId = m_appId.trim();
            log.info("App ID is set to {} by APP_ID property from OS environment variable", m_appId);
            return;
        }

        // 3. Try to get app id from app.properties.
        m_appId = m_appProperties.getProperty("app.id");
        if (!Utils.isBlank(m_appId)) {
            m_appId = m_appId.trim();
            log.info("App ID is set to {} by app.id property from {}", m_appId, APP_PROPERTIES_CLASSPATH);
            return;
        }

        m_appId = null;
        log.warn("app.id is not available from System Property and {}. It is set to null", APP_PROPERTIES_CLASSPATH);
    }

    @Override
    public Class<? extends Provider> getType() {
        return null;
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        return null;
    }

    @Override
    public void initialize() {
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(APP_PROPERTIES_CLASSPATH);
            if (in == null) {
                in = DefaultApplicationProvider.class.getResourceAsStream(APP_PROPERTIES_CLASSPATH);
            }

            initialize(in);
        } catch (Throwable ex) {
            log.error("Initialize DefaultApplicationProvider failed.", ex);
        }
    }
}
