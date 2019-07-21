package lucky.apollo.component;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.config.RefreshablePropertySource;
import lucky.apollo.entity.po.ServerConfigPO;
import lucky.apollo.repository.ServerConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
@Component
@Slf4j
public class PortalPropertySourceRefresher extends RefreshablePropertySource {
    @Autowired
    private ServerConfigRepository serverConfigRepository;

    private PortalPropertySourceRefresher(String name, Map<String, Object> source) {
        super(name, source);
    }

    public PortalPropertySourceRefresher() {
        super("DBConfig", Maps.newConcurrentMap());
    }


    @Override
    protected void refresh() {
        Iterable<ServerConfigPO> dbConfigs = serverConfigRepository.findAll();
        for (ServerConfigPO config : dbConfigs) {
            String key = config.getKey();
            Object value = config.getValue();

            if (!Objects.equals(this.source.get(key), value)) {
                log.info("Load config from DB : {} = {}. Old value = {}", key,
                        value, this.source.get(key));
            }
            this.source.put(key, value);
        }
    }
}