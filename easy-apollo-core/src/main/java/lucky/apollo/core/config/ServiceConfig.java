package lucky.apollo.core.config;

import lucky.apollo.common.config.RefreshableConfig;
import lucky.apollo.common.config.RefreshablePropertySource;
import org.springframework.stereotype.Component;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Component
public class ServiceConfig extends RefreshableConfig {


    @Override
    protected RefreshablePropertySource getRefreshablePropertySource() {
        return null;
    }
}