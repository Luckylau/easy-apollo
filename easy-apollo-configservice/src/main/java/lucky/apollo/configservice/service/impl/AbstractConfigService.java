package lucky.apollo.configservice.service.impl;

import com.google.common.base.Strings;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.entity.dto.ApolloNotificationMessageDTO;
import lucky.apollo.configservice.service.ConfigService;
import lucky.apollo.core.entity.ReleasePO;
import lucky.apollo.core.grayReleaseRule.GrayReleaseRulesHolder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @Author luckylau
 * @Date 2019/12/4
 */
public abstract class AbstractConfigService implements ConfigService {

    @Autowired
    private GrayReleaseRulesHolder grayReleaseRulesHolder;

    @Override
    public ReleasePO loadConfig(String clientAppId, String clientIp, String configAppId, String configClusterName,
                                String configNamespace, String dataCenter, ApolloNotificationMessageDTO clientMessages) {
        // load from specified cluster fist
        if (!Objects.equals(ConfigConsts.CLUSTER_NAME_DEFAULT, configClusterName)) {
            ReleasePO clusterRelease = findRelease(clientAppId, clientIp, configAppId, configClusterName, configNamespace,
                    clientMessages);

            if (!Objects.isNull(clusterRelease)) {
                return clusterRelease;
            }
        }

        // try to load via data center
        if (!Strings.isNullOrEmpty(dataCenter) && !Objects.equals(dataCenter, configClusterName)) {
            ReleasePO dataCenterRelease = findRelease(clientAppId, clientIp, configAppId, dataCenter, configNamespace,
                    clientMessages);
            if (!Objects.isNull(dataCenterRelease)) {
                return dataCenterRelease;
            }
        }

        // fallback to default release
        return findRelease(clientAppId, clientIp, configAppId, ConfigConsts.CLUSTER_NAME_DEFAULT, configNamespace,
                clientMessages);
    }

    /**
     * Find release
     *
     * @param clientAppId       the client's app id
     * @param clientIp          the client ip
     * @param configAppId       the requested config's app id
     * @param configClusterName the requested config's cluster name
     * @param configNamespace   the requested config's namespace name
     * @param clientMessages    the messages received in client side
     * @return the release
     */
    private ReleasePO findRelease(String clientAppId, String clientIp, String configAppId, String configClusterName,
                                  String configNamespace, ApolloNotificationMessageDTO clientMessages) {
        Long grayReleaseId = grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule(clientAppId, clientIp, configAppId,
                configClusterName, configNamespace);

        ReleasePO release = null;

        if (grayReleaseId != null) {
            release = findActiveOne(grayReleaseId, clientMessages);
        }

        if (release == null) {
            release = findLatestActiveRelease(configAppId, configClusterName, configNamespace, clientMessages);
        }

        return release;
    }

    /**
     * Find active release by id
     */
    protected abstract ReleasePO findActiveOne(long id, ApolloNotificationMessageDTO clientMessages);

    /**
     * Find active release by app id, cluster name and namespace name
     */
    protected abstract ReleasePO findLatestActiveRelease(String configAppId, String configClusterName,
                                                         String configNamespaceName, ApolloNotificationMessageDTO clientMessages);
}
