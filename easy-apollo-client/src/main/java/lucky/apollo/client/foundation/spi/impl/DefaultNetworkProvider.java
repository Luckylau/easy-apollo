package lucky.apollo.client.foundation.spi.impl;

import lucky.apollo.client.foundation.spi.NetworkProvider;
import lucky.apollo.client.foundation.spi.Provider;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

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
        return NetworkInterfaceManager.INSTANCE.getLocalHostName();
    }

    @Override
    public Class<? extends Provider> getType() {
        return NetworkProvider.class;
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


    private enum NetworkInterfaceManager {
        /**
         * 获取网卡信息
         */
        INSTANCE;

        private InetAddress m_local;

        NetworkInterfaceManager() {
            load();
        }

        public String getLocalHostAddress() {
            return m_local.getHostAddress();
        }

        public String getLocalHostName() {
            return m_local.getHostName();
        }

        private void load() {
            String ip = getProperty("host.ip");

            if (ip != null) {
                try {
                    m_local = InetAddress.getByName(ip);
                    return;
                } catch (Exception e) {
                    System.err.println(e);
                    // ignore
                }
            }

            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                List<NetworkInterface> nis = interfaces == null ? Collections.<NetworkInterface>emptyList() : Collections.list(interfaces);
                List<InetAddress> addresses = new ArrayList<InetAddress>();
                InetAddress local = null;

                try {
                    for (NetworkInterface ni : nis) {
                        if (ni.isUp() && !ni.isLoopback()) {
                            addresses.addAll(Collections.list(ni.getInetAddresses()));
                        }
                    }
                    local = findValidateIp(addresses);
                } catch (Exception e) {
                    // ignore
                }
                if (local != null) {
                    m_local = local;
                    return;
                }
            } catch (SocketException e) {
                // ignore it
            }

            m_local = InetAddress.getLoopbackAddress();


        }

        private String getProperty(String name) {
            String value = null;

            value = System.getProperty(name);

            if (value == null) {
                value = System.getenv(name);
            }

            return value;
        }

        private InetAddress findValidateIp(List<InetAddress> addresses) {
            InetAddress local = null;
            int maxWeight = -1;
            for (InetAddress address : addresses) {
                if (address instanceof Inet4Address) {
                    int weight = 0;

                    if (address.isSiteLocalAddress()) {
                        weight += 8;
                    }

                    if (address.isLinkLocalAddress()) {
                        weight += 4;
                    }

                    if (address.isLoopbackAddress()) {
                        weight += 2;
                    }

                    // has host name
                    // TODO fix performance issue when calling getHostName
                    if (!Objects.equals(address.getHostName(), address.getHostAddress())) {
                        weight += 1;
                    }

                    if (weight > maxWeight) {
                        maxWeight = weight;
                        local = address;
                    }
                }
            }
            return local;
        }

    }


}
