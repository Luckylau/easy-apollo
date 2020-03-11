package lucky.apollo.common.service;

import lucky.apollo.common.constant.Env;
import org.springframework.core.Ordered;

/**
 * @Author luckylau
 * @Date 2020/3/11
 */
public interface MetaServerProvider extends Ordered {
    /**
     * Provide the Apollo meta server address, could be a domain url or comma separated ip addresses, like http://1.2.3.4:8080,http://2.3.4.5:8080.
     * <br/>
     * In production environment, we suggest using one single domain like http://config.xxx.com(backed by software load balancers like nginx) instead of multiple ip addresses
     */
    String getMetaServerAddress(Env targetEnv);
}
