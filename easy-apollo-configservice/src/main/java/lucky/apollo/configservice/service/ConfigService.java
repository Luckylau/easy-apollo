package lucky.apollo.configservice.service;

import lucky.apollo.common.entity.dto.ApolloNotificationMessageDTO;
import lucky.apollo.core.entity.ReleasePO;
import lucky.apollo.core.message.ReleaseMessageListener;

/**
 * @Author luckylau
 * @Date 2019/12/4
 */
public interface ConfigService extends ReleaseMessageListener {

    /**
     * Load config
     *
     * @param clientAppId       the client's app id
     * @param clientIp          the client ip
     * @param configAppId       the requested config's app id
     * @param configClusterName the requested config's cluster name
     * @param configNamespace   the requested config's namespace name
     * @param dataCenter        the client data center
     * @param clientMessages    the messages received in client side
     * @return the Release
     */
    ReleasePO loadConfig(String clientAppId, String clientIp, String configAppId, String
            configClusterName, String configNamespace, String dataCenter, ApolloNotificationMessageDTO clientMessages);

}
