package lucky.apollo.client.spring.boot;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.config.Config;
import lucky.apollo.client.service.ConfigService;
import lucky.apollo.client.spring.config.ConfigPropertySourceFactory;
import lucky.apollo.client.spring.config.PropertySourcesConstants;
import lucky.apollo.client.spring.util.SpringInjector;
import lucky.apollo.common.constant.ConfigConsts;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2022/3/6
 */
@Slf4j
public class ApolloApplicationContextInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Splitter NAMESPACE_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
    private static final String[] APOLLO_SYSTEM_PROPERTIES = {"app.id", ConfigConsts.APOLLO_CLUSTER_KEY,
            "apollo.cacheDir", ConfigConsts.APOLLO_META_KEY};

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
            .getInstance(ConfigPropertySourceFactory.class);

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();

        initializeSystemProperty(environment);

        String enabled = environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED, "false");
        if (!Boolean.parseBoolean(enabled)) {
            log.debug("Apollo bootstrap config is not enabled for context {}, see property: ${{}}", context, PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED);
            return;
        }
        log.debug("Apollo bootstrap config is enabled for context {}", context);

        if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            //already initialized
            return;
        }

        String namespaces = environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES, ConfigConsts.NAMESPACE_APPLICATION);
        log.debug("Apollo bootstrap namespaces: {}", namespaces);
        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespaces);

        CompositePropertySource composite = new CompositePropertySource(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
        for (String namespace : namespaceList) {
            Config config = ConfigService.getConfig(namespace);

            composite.addPropertySource(configPropertySourceFactory.getConfigPropertySource(namespace, config));
        }

        environment.getPropertySources().addFirst(composite);
    }

    void initializeSystemProperty(ConfigurableEnvironment environment) {
        for (String propertyName : APOLLO_SYSTEM_PROPERTIES) {
            fillSystemPropertyFromEnvironment(environment, propertyName);
        }
    }

    private void fillSystemPropertyFromEnvironment(ConfigurableEnvironment environment, String propertyName) {
        if (System.getProperty(propertyName) != null) {
            return;
        }

        String propertyValue = environment.getProperty(propertyName);

        if (Strings.isNullOrEmpty(propertyValue)) {
            return;
        }

        System.setProperty(propertyName, propertyValue);
    }
}
