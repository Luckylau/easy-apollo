package lucky.apollo.client.spring.config;

import lucky.apollo.client.config.Config;
import lucky.apollo.client.config.ConfigChangeListener;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.Set;

/**
 * @Author luckylau
 * @Date 2021/2/21
 */
public class ConfigPropertySource extends EnumerablePropertySource<Config> {

    private static final String[] EMPTY_ARRAY = new String[0];

    ConfigPropertySource(String name, Config source) {
        super(name, source);
    }

    @Override
    public String[] getPropertyNames() {
        Set<String> propertyNames = this.source.getPropertyNames();
        if (propertyNames.isEmpty()) {
            return EMPTY_ARRAY;
        }
        return propertyNames.toArray(new String[propertyNames.size()]);
    }

    @Override
    public Object getProperty(String name) {
        return this.source.getProperty(name, null);

    }

    public void addChangeListener(ConfigChangeListener listener) {
        this.source.addChangeListener(listener);
    }


}
