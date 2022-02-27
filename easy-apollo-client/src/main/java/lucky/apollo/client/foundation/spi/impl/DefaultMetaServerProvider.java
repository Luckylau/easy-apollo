package lucky.apollo.client.foundation.spi.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.client.foundation.Foundation;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.constant.Env;
import lucky.apollo.common.service.MetaServerProvider;

/**
 * @Author luckylau
 * @Date 2022/2/27
 */
@Slf4j
public class DefaultMetaServerProvider implements MetaServerProvider {

    public static final int ORDER = 0;

    private final String metaServerAddress;

    public DefaultMetaServerProvider() {
        metaServerAddress = initMetaServerAddress();
    }

    private String initMetaServerAddress() {
        // 1. Get from System Property
        String metaAddress = System.getProperty(ConfigConsts.APOLLO_META_KEY);
        if (Strings.isNullOrEmpty(metaAddress)) {
            // 2. Get from OS environment variable, which could not contain dot and is normally in UPPER case
            metaAddress = System.getenv("APOLLO_META");
        }
        if (Strings.isNullOrEmpty(metaAddress)) {
            // 3. Get from server.properties
            metaAddress = Foundation.server().getProperty(ConfigConsts.APOLLO_META_KEY, null);
        }
        if (Strings.isNullOrEmpty(metaAddress)) {
            // 4. Get from app.properties
            metaAddress = Foundation.app().getProperty(ConfigConsts.APOLLO_META_KEY, null);
        }

        if (Strings.isNullOrEmpty(metaAddress)) {
            log.warn("Could not find meta server address, because it is not available in neither (1) JVM system property 'apollo.meta', (2) OS env variable 'APOLLO_META' (3) property 'apollo.meta' from server.properties nor (4) property 'apollo.meta' from app.properties");
        } else {
            metaAddress = metaAddress.trim();
            log.info("Located meta services from apollo.meta configuration: {}!", metaAddress);
        }

        return metaAddress;
    }

    @Override
    public String getMetaServerAddress(Env targetEnv) {
        //for default meta server provider, we don't care the actual environment
        return metaServerAddress;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
