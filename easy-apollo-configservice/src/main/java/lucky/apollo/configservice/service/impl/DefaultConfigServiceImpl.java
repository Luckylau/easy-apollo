package lucky.apollo.configservice.service.impl;

import lucky.apollo.common.entity.dto.ApolloNotificationMessageDTO;
import lucky.apollo.core.entity.ReleaseMessagePO;
import lucky.apollo.core.entity.ReleasePO;
import lucky.apollo.core.service.ReleaseService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author luckylau
 * @Date 2019/12/6
 */
public class DefaultConfigServiceImpl extends AbstractConfigService {

    @Autowired
    private ReleaseService releaseService;

    @Override
    protected ReleasePO findActiveOne(long id, ApolloNotificationMessageDTO clientMessages) {
        return releaseService.findActiveOne(id);
    }

    @Override
    protected ReleasePO findLatestActiveRelease(String configAppId, String configClusterName, String configNamespaceName, ApolloNotificationMessageDTO clientMessages) {
        return releaseService.findLatestActiveRelease(configAppId, configClusterName,
                configNamespaceName);
    }

    @Override
    public void handleMessage(ReleaseMessagePO message, String channel) {
        //since there is no cache, so do nothing
    }
}
