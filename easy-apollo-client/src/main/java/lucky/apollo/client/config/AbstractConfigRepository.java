package lucky.apollo.client.config;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.util.ExceptionUtil;

import java.util.List;
import java.util.Properties;

/**
 * @Author luckylau
 * @Date 2020/9/20
 */
@Slf4j
public abstract class AbstractConfigRepository implements ConfigRepository {
    private List<RepositoryChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();

    protected boolean trySync() {
        try {
            sync();
            return true;
        } catch (Throwable ex) {
            log.warn("Sync config failed, will retry. Repository {}, reason: {}", this.getClass(), ExceptionUtil
                            .getDetailMessage(ex));
        }
        return false;
    }

    protected abstract void sync();

    @Override
    public void addChangeListener(RepositoryChangeListener listener) {
        if (!m_listeners.contains(listener)) {
            m_listeners.add(listener);
        }
    }

    @Override
    public void removeChangeListener(RepositoryChangeListener listener) {
        m_listeners.remove(listener);
    }

    protected void fireRepositoryChange(String namespace, Properties newProperties) {
        for (RepositoryChangeListener listener : m_listeners) {
            try {
                listener.onRepositoryChange(namespace, newProperties);
            } catch (Throwable ex) {
                log.error("Failed to invoke repository change listener {}", listener.getClass(), ex);
            }
        }
    }
}
