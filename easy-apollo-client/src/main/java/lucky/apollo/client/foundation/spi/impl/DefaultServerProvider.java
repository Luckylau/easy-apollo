package lucky.apollo.client.foundation.spi.impl;

import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.foundation.io.BOMInputStream;
import lucky.apollo.client.foundation.spi.Provider;
import lucky.apollo.client.foundation.spi.ServerProvider;
import lucky.apollo.client.util.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @Author liuJun
 * @Date 2019/12/26
 */
@Slf4j
public class DefaultServerProvider implements ServerProvider {

    private static final String SERVER_PROPERTIES_LINUX = "/opt/settings/server.properties";
    private static final String SERVER_PROPERTIES_WINDOWS = "C:/opt/settings/server.properties";

    private String m_env;
    private String m_dc;

    private Properties m_serverProperties = new Properties();


    @Override
    public String getEnvType() {
        return null;
    }

    @Override
    public boolean isEnvTypeSet() {
        return false;
    }

    @Override
    public String getDataCenter() {
        return null;
    }

    @Override
    public boolean isDataCenterSet() {
        return false;
    }

    @Override
    public void initialize(InputStream in) throws IOException {
        try {
            if (in != null) {
                try {
                    m_serverProperties.load(new InputStreamReader(new BOMInputStream(in), StandardCharsets.UTF_8));
                } finally {
                    in.close();
                }
            }

            initEnvType();
            initDataCenter();
        } catch (Throwable ex) {
            log.error("Initialize DefaultServerProvider failed.", ex);
        }
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
            String path = Utils.isOSWindows() ? SERVER_PROPERTIES_WINDOWS : SERVER_PROPERTIES_LINUX;

            File file = new File(path);
            if (file.exists() && file.canRead()) {
                log.info("Loading {}", file.getAbsolutePath());
                FileInputStream fis = new FileInputStream(file);
                initialize(fis);
                return;
            }

            initialize(null);
        } catch (Throwable ex) {
            log.error("Initialize DefaultServerProvider failed.", ex);
        }
    }
}
