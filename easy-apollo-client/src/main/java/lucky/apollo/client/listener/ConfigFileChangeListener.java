package lucky.apollo.client.listener;

import lucky.apollo.client.model.ConfigFileChangeEvent;

/**
 * @Author liuJun
 * @Date 2019/12/13
 */
public interface ConfigFileChangeListener {
    /**
     * Invoked when there is any config change for the namespace.
     *
     * @param changeEvent the event for this change
     */
    void onChange(ConfigFileChangeEvent changeEvent);
}
