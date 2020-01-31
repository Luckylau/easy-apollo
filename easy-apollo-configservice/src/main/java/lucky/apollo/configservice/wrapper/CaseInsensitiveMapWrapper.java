package lucky.apollo.configservice.wrapper;

import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/12/6
 */
public class CaseInsensitiveMapWrapper<T> {
    private final Map<String, T> delegate;

    public CaseInsensitiveMapWrapper(Map<String, T> delegate) {
        this.delegate = delegate;
    }

    public T get(String key) {
        return delegate.get(key.toLowerCase());
    }

    public T put(String key, T value) {
        return delegate.put(key.toLowerCase(), value);
    }

    public T remove(String key) {
        return delegate.remove(key.toLowerCase());
    }
}