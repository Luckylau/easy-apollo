package lucky.apollo.portal.config;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.config.RefreshableConfig;
import lucky.apollo.common.config.RefreshablePropertySource;
import lucky.apollo.common.constant.Env;
import lucky.apollo.portal.component.PortalPropertySourceRefresher;
import lucky.apollo.portal.constant.ServerConfigKey;
import lucky.apollo.portal.entity.vo.Organization;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
@Service
@Slf4j
public class PortalConfig extends RefreshableConfig implements ApplicationContextAware {

    private static final Type ORGANIZATION = new TypeToken<List<Organization>>() {
    }.getType();

    private Gson gson = new Gson();

    private static ApplicationContext applicationContext = null;

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

    public List<Organization> organizations() {

        String organizations = getValue("organizations");
        return organizations == null ? Collections.emptyList() : gson.fromJson(organizations, ORGANIZATION);
    }

    public String wikiAddress() {
        return getValue("wiki.address", "https://github.com/ctripcorp/apollo/wiki");
    }

    public Env getActiveEnv() {
        String env = applicationContext.getEnvironment().getActiveProfiles()[0];
        return Env.fromString(env);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}