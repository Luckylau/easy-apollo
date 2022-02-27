package lucky.apollo.client.config.impl.configFile;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.config.ConfigChangeListener;
import lucky.apollo.client.config.ConfigFile;
import lucky.apollo.client.config.ConfigRepository;
import lucky.apollo.client.config.RepositoryChangeListener;
import lucky.apollo.client.constant.ConfigSourceType;
import lucky.apollo.client.enums.PropertyChangeType;
import lucky.apollo.client.listener.ConfigFileChangeListener;
import lucky.apollo.client.model.ConfigFileChangeEvent;
import lucky.apollo.client.util.ExceptionUtil;
import lucky.apollo.common.utils.ApolloThreadFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author luckylau
 * @Date 2022/2/27
 */
@Slf4j
public abstract class AbstractConfigFile implements ConfigFile, RepositoryChangeListener {
    private static ExecutorService m_executorService;

    static {
        m_executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), ApolloThreadFactory
                .create("ConfigFile", true));
    }

    protected final ConfigRepository m_configRepository;
    protected final String m_namespace;
    protected final AtomicReference<Properties> m_configProperties;
    private final List<ConfigFileChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();
    private volatile ConfigSourceType m_sourceType = ConfigSourceType.NONE;

    public AbstractConfigFile(String namespace, ConfigRepository configRepository) {
        m_configRepository = configRepository;
        m_namespace = namespace;
        m_configProperties = new AtomicReference<>();
        initialize();
    }

    private void initialize() {
        try {
            m_configProperties.set(m_configRepository.getConfig());
            m_sourceType = m_configRepository.getSourceType();
        } catch (Throwable ex) {
            log.warn("Init Apollo Config File failed - namespace: {}, reason: {}.",
                    m_namespace, ExceptionUtil.getDetailMessage(ex));
        } finally {
            //register the change listener no matter config repository is working or not
            //so that whenever config repository is recovered, config could get changed
            m_configRepository.addChangeListener(this);
        }
    }

    @Override
    public String getNamespace() {
        return m_namespace;
    }

    @Override
    public void addChangeListener(ConfigFileChangeListener listener) {
        if (!m_listeners.contains(listener)) {
            m_listeners.add(listener);
        }
    }

    @Override
    public boolean removeChangeListener(ConfigChangeListener listener) {
        return m_listeners.remove(listener);
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
        Properties newConfigProperties = new Properties();
        newConfigProperties.putAll(newProperties);

        String oldValue = getContent();

        update(newProperties);
        m_sourceType = m_configRepository.getSourceType();

        String newValue = getContent();

        PropertyChangeType changeType = PropertyChangeType.MODIFIED;

        if (oldValue == null) {
            changeType = PropertyChangeType.ADDED;
        } else if (newValue == null) {
            changeType = PropertyChangeType.DELETED;
        }

        this.fireConfigChange(new ConfigFileChangeEvent(m_namespace, oldValue, newValue, changeType));
    }

    private void fireConfigChange(final ConfigFileChangeEvent changeEvent) {
        for (final ConfigFileChangeListener listener : m_listeners) {
            m_executorService.submit(() -> {
                String listenerName = listener.getClass().getName();
                try {
                    listener.onChange(changeEvent);
                } catch (Throwable ex) {
                    log.error("Failed to invoke config file change listener {}", listenerName, ex);
                }
            });
        }
    }


    protected abstract void update(Properties newProperties);
}
