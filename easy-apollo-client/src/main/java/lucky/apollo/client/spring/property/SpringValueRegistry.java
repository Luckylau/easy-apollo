package lucky.apollo.client.spring.property;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.BeanFactory;

import java.util.Collection;
import java.util.Map;

/**
 * @Author luckylau
 * @Date 2021/2/21
 */
public class SpringValueRegistry {
    private final Map<BeanFactory, Multimap<String, SpringValue>> registry = Maps.newConcurrentMap();
    private final Object LOCK = new Object();

    public void register(BeanFactory beanFactory, String key, SpringValue springValue) {
        if (!registry.containsKey(beanFactory)) {
            synchronized (LOCK) {
                if (!registry.containsKey(beanFactory)) {
                    registry.put(beanFactory, LinkedListMultimap.<String, SpringValue>create());
                }
            }
        }

        registry.get(beanFactory).put(key, springValue);
    }

    public Collection<SpringValue> get(BeanFactory beanFactory, String key) {
        Multimap<String, SpringValue> beanFactorySpringValues = registry.get(beanFactory);
        if (beanFactorySpringValues == null) {
            return null;
        }
        return beanFactorySpringValues.get(key);
    }
}
