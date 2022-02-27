package lucky.apollo.client.config.impl.configFile;

import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.config.ConfigRepository;
import lucky.apollo.client.exception.ApolloConfigException;
import lucky.apollo.client.util.ExceptionUtil;
import lucky.apollo.common.constant.ConfigFileFormat;
import lucky.apollo.common.utils.PropertiesUtil;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author luckylau
 * @Date 2022/2/27
 */
@Slf4j
public class PropertiesConfigFile extends AbstractConfigFile {

    protected AtomicReference<String> m_contentCache;

    public PropertiesConfigFile(String namespace,
                                ConfigRepository configRepository) {
        super(namespace, configRepository);
        m_contentCache = new AtomicReference<>();
    }

    @Override
    protected void update(Properties newProperties) {
        m_configProperties.set(newProperties);
        m_contentCache.set(null);
    }

    @Override
    public String getContent() {
        if (m_contentCache.get() == null) {
            m_contentCache.set(doGetContent());
        }
        return m_contentCache.get();
    }

    String doGetContent() {
        if (!this.hasContent()) {
            return null;
        }

        try {
            return PropertiesUtil.toString(m_configProperties.get());
        } catch (Throwable ex) {
            throw new ApolloConfigException(String
                    .format("Parse properties file content failed for namespace: %s, cause: %s",
                            m_namespace, ExceptionUtil.getDetailMessage(ex)));
        }
    }

    @Override
    public boolean hasContent() {
        return m_configProperties.get() != null && !m_configProperties.get().isEmpty();
    }

    @Override
    public ConfigFileFormat getConfigFileFormat() {
        return ConfigFileFormat.Properties;
    }
}
