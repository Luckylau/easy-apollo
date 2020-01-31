package lucky.apollo.client.foundation.spi.impl;

import lucky.apollo.client.foundation.spi.NetworkProvider;
import lucky.apollo.client.foundation.spi.Provider;

/**
 * @Author luckylau
 * @Date 2019/12/18
 */
public class DefaultNetworkProvider implements NetworkProvider {


    @Override
    public String getHostAddress() {
        return NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
    }

    @Override
    public String getHostName() {
        return null;
    }

    @Override
    public Class<? extends Provider> getType() {
        return null;
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        if ("host.address".equalsIgnoreCase(name)) {
            String val = getHostAddress();
            return val == null ? defaultValue : val;
        } else if ("host.name".equalsIgnoreCase(name)) {
            String val = getHostName();
            return val == null ? defaultValue : val;
        } else {
            return defaultValue;
        }
    }

    @Override
    public void initialize() {

    }
}
