package lucky.apollo.core.component;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.config.RefreshablePropertySource;
import lucky.apollo.core.entity.ServerConfigPO;
import lucky.apollo.core.repository.ServerConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * @Author luckylau
 * @Date 2019/9/18
 */
@Slf4j
@Component
public class ServicePropertySourceRefresher extends RefreshablePropertySource {

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    private ServicePropertySourceRefresher(String name, Map<String, Object> source) {
        super(name, source);
    }

    public ServicePropertySourceRefresher() {
        super("DBConfig", Maps.newConcurrentMap());
    }

    @Override
    protected void refresh() {
        Iterable<ServerConfigPO> serverConfigDB = serverConfigRepository.findAll();
        for (ServerConfigPO config : serverConfigDB) {
            String key = config.getKey();
            Object value = config.getValue();
            if (this.source.get(key) == null) {
                log.info("service Load config from DB : {} = {}", key, value);
            } else {
                if (!Objects.equals(this.source.get(key), value)) {
                    log.info("service Load config from DB : {} = {}. Old value = {}", key,
                            value, this.source.get(key));
                }
            }
            this.source.put(key, value);
        }
    }
}
