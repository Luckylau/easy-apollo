package lucky.apollo.config;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.component.PortalPropertySourceRefresher;
import lucky.apollo.constant.ServerConfigKey;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
@Service
@Slf4j
public class PortalConfig extends RefreshableConfig {

    private final PortalPropertySourceRefresher portalPropertySourceRefresher;

    public PortalConfig(PortalPropertySourceRefresher portalPropertySourceRefresher) {
        this.portalPropertySourceRefresher = portalPropertySourceRefresher;
    }

    @Override
    protected RefreshablePropertySource getRefreshablePropertySource() {
        return portalPropertySourceRefresher;
    }

    public List<String> superAdmins() {
        String superAdminConfig = getValue(ServerConfigKey.SUPER_ADMIN, "");
        if (Strings.isNullOrEmpty(superAdminConfig)) {
            return Collections.emptyList();
        }
        return splitter.splitToList(superAdminConfig);
    }


}