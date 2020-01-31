package lucky.apollo.client.foundation;

import com.google.common.collect.Lists;
import org.springframework.core.Ordered;

import java.util.*;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
public class ServiceBootstrap {
    public static <S> S loadFirst(Class<S> clazz) {
        Iterator<S> iterator = loadAll(clazz);
        if (!iterator.hasNext()) {
            throw new IllegalStateException(String.format(
                    "No implementation defined in /META-INF/services/%s, please check whether the file exists and has the right implementation class!",
                    clazz.getName()));
        }
        return iterator.next();
    }

    public static <S> Iterator<S> loadAll(Class<S> clazz) {
        ServiceLoader<S> loader = ServiceLoader.load(clazz);

        return loader.iterator();
    }

    public static <S extends Ordered> List<S> loadAllOrdered(Class<S> clazz) {
        Iterator<S> iterator = loadAll(clazz);

        if (!iterator.hasNext()) {
            throw new IllegalStateException(String.format(
                    "No implementation defined in /META-INF/services/%s, please check whether the file exists and has the right implementation class!",
                    clazz.getName()));
        }

        List<S> candidates = Lists.newArrayList(iterator);
        Collections.sort(candidates, new Comparator<S>() {
            @Override
            public int compare(S o1, S o2) {
                // the smaller order has higher priority
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });

        return candidates;
    }

    public static <S extends Ordered> S loadPrimary(Class<S> clazz) {
        List<S> candidates = loadAllOrdered(clazz);

        return candidates.get(0);
    }
}
