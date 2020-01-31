package lucky.apollo.client.config;

import lucky.apollo.client.model.ConfigChangeEvent;

/**
 * @Author liuJun
 * @Date 2019/12/13
 */
public interface ConfigChangeListener {
    /**
     * Invoked when there is any config change for the namespace.
     *
     * @param changeEvent the event for this change
     */
    public void onChange(ConfigChangeEvent changeEvent);
}
