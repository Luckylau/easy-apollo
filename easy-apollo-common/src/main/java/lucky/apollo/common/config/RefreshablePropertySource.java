package lucky.apollo.common.config;

import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
public abstract class RefreshablePropertySource extends MapPropertySource {
    public RefreshablePropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    @Override
    public Object getProperty(String name) {
        return this.source.get(name);
    }

    /**
     * 刷新属性
     */
    protected abstract void refresh();
}