package lucky.apollo.client.config.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.config.AbstractConfig;
import lucky.apollo.client.config.ConfigRepository;
import lucky.apollo.client.config.RepositoryChangeListener;
import lucky.apollo.client.constant.ConfigSourceType;
import lucky.apollo.client.enums.PropertyChangeType;
import lucky.apollo.client.model.ConfigChange;
import lucky.apollo.client.model.ConfigChangeEvent;
import lucky.apollo.client.util.ExceptionUtil;
import lucky.apollo.common.utils.ClassLoaderUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author luckylau
 * @Date 2020/9/19
 */
@Slf4j
public class DefaultConfig extends AbstractConfig implements RepositoryChangeListener {

    private final String m_namespace;
    private final Properties m_resourceProperties;
    private final AtomicReference<Properties> m_configProperties;
    private final ConfigRepository m_configRepository;
    private final RateLimiter m_warnLogRateLimiter;

    private volatile ConfigSourceType m_sourceType = ConfigSourceType.NONE;

    public DefaultConfig(String namespace, ConfigRepository configRepository) {
        m_namespace = namespace;
        m_resourceProperties = loadFromResource(m_namespace);
        m_configRepository = configRepository;
        m_configProperties = new AtomicReference<>();
        // 1 warning log output per minute
        m_warnLogRateLimiter = RateLimiter.create(0.017);
        initialize();
    }

    private void initialize() {
        try {
            updateConfig(m_configRepository.getConfig(), m_configRepository.getSourceType());
        } catch (Throwable ex) {
            log.warn("Init Apollo Local Config failed - namespace: {}, reason: {}.",
                    m_namespace, ExceptionUtil.getDetailMessage(ex));
        } finally {
            //register the change listener no matter config repository is working or not
            //so that whenever config repository is recovered, config could get changed
            m_configRepository.addChangeListener(this);
        }
    }

    private void updateConfig(Properties newConfigProperties, ConfigSourceType sourceType) {
        m_configProperties.set(newConfigProperties);
        m_sourceType = sourceType;
    }

    private Properties loadFromResource(String namespace) {
        String name = String.format("META-INF/config/%s.properties", namespace);
        InputStream in = ClassLoaderUtil.getLoader().getResourceAsStream(name);
        Properties properties = null;

        if (in != null) {
            properties = new Properties();

            try {
                properties.load(in);
            } catch (IOException ex) {
                log.error("Load resource config for namespace {} failed", namespace, ex);
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }

        return properties;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        // step 1: check system properties, i.e. -Dkey= value
        String value = System.getProperty(key);

        // step 2: check local cached properties file
        if (value == null && m_configProperties.get() != null) {
            value = m_configProperties.get().getProperty(key);
        }

        /**
         * step 3: check env variable, i.e. PATH=...
         * normally system environment variables are in UPPERCASE, however there might be exceptions.
         * so the caller should provide the key in the right case
         */
        if (value == null) {
            value = System.getenv(key);
        }

        // step 4: check properties file from classpath
        if (value == null && m_resourceProperties != null) {
            value = (String) m_resourceProperties.get(key);
        }

        if (value == null && m_configProperties.get() == null && m_warnLogRateLimiter.tryAcquire()) {
            log.warn("Could not load config for namespace {} from Apollo, please check whether the configs are released in Apollo! Return default value now!", m_namespace);
        }

        return value == null ? defaultValue : value;
    }


    @Override
    public Set<String> getPropertyNames() {
        Properties properties = m_configProperties.get();
        if (properties == null) {
            return Collections.emptySet();
        }

        return stringPropertyNames(properties);
    }

    private Set<String> stringPropertyNames(Properties properties) {
        //jdk9以下版本Properties#enumerateStringProperties方法存在性能问题，keys() + get(k) 重复迭代, jdk9之后改为entrySet遍历.
        Map<String, String> h = new HashMap<>();
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            Object k = e.getKey();
            Object v = e.getValue();
            if (k instanceof String && v instanceof String) {
                h.put((String) k, (String) v);
            }
        }
        return h.keySet();
    }

    @Override
    public ConfigSourceType getSourceType() {
        return m_sourceType;
    }

    @Override
    public void onRepositoryChange(String namespace, Properties newProperties) {
        if (newProperties.equals(m_configProperties.get())) {
            return;
        }

        ConfigSourceType sourceType = m_configRepository.getSourceType();
        Properties newConfigProperties = new Properties();
        newConfigProperties.putAll(newProperties);

        Map<String, ConfigChange> actualChanges = updateAndCalcConfigChanges(newConfigProperties, sourceType);

        //check double checked result
        if (actualChanges.isEmpty()) {
            return;
        }

        this.fireConfigChange(new ConfigChangeEvent(m_namespace, actualChanges));

    }

    private Map<String, ConfigChange> updateAndCalcConfigChanges(Properties newConfigProperties,
                                                                 ConfigSourceType sourceType) {
        List<ConfigChange> configChanges =
                calcPropertyChanges(m_namespace, m_configProperties.get(), newConfigProperties);

        ImmutableMap.Builder<String, ConfigChange> actualChanges =
                new ImmutableMap.Builder<>();

        /** === Double check since DefaultConfig has multiple config sources ==== **/

        //1. use getProperty to update configChanges's old value
        for (ConfigChange change : configChanges) {
            change.setOldValue(this.getProperty(change.getPropertyName(), change.getOldValue()));
        }

        //2. update m_configProperties
        updateConfig(newConfigProperties, sourceType);
        clearConfigCache();

        //3. use getProperty to update configChange's new value and calc the final changes
        for (ConfigChange change : configChanges) {
            change.setNewValue(this.getProperty(change.getPropertyName(), change.getNewValue()));
            switch (change.getChangeType()) {
                case ADDED:
                    if (Objects.equals(change.getOldValue(), change.getNewValue())) {
                        break;
                    }
                    if (change.getOldValue() != null) {
                        change.setChangeType(PropertyChangeType.MODIFIED);
                    }
                    actualChanges.put(change.getPropertyName(), change);
                    break;
                case MODIFIED:
                    if (!Objects.equals(change.getOldValue(), change.getNewValue())) {
                        actualChanges.put(change.getPropertyName(), change);
                    }
                    break;
                case DELETED:
                    if (Objects.equals(change.getOldValue(), change.getNewValue())) {
                        break;
                    }
                    if (change.getNewValue() != null) {
                        change.setChangeType(PropertyChangeType.MODIFIED);
                    }
                    actualChanges.put(change.getPropertyName(), change);
                    break;
                default:
                    //do nothing
                    break;
            }
        }
        return actualChanges.build();
    }
}
