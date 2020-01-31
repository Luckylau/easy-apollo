package lucky.apollo.configservice.component;

import com.google.common.base.Strings;
import com.google.common.collect.Queues;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

/**
 * @Author luckylau
 * @Date 2019/12/6
 */
@Service
public class InstanceConfigAuditService implements InitializingBean {

    private static final int INSTANCE_CONFIG_AUDIT_MAX_SIZE = 10000;


    private BlockingQueue<InstanceConfigAuditModel> audits = Queues.newLinkedBlockingQueue
            (INSTANCE_CONFIG_AUDIT_MAX_SIZE);

    public boolean audit(String appId, String clusterName, String dataCenter, String
            ip, String configAppId, String configClusterName, String configNamespace, String releaseKey) {
        return this.audits.offer(new InstanceConfigAuditModel(appId, clusterName, dataCenter, ip,
                configAppId, configClusterName, configNamespace, releaseKey));
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Getter
    @Setter
    private static class InstanceConfigAuditModel {
        private String appId;
        private String clusterName;
        private String dataCenter;
        private String ip;
        private String configAppId;
        private String configClusterName;
        private String configNamespace;
        private String releaseKey;
        private Date offerTime;

        public InstanceConfigAuditModel(String appId, String clusterName, String dataCenter, String
                clientIp, String configAppId, String configClusterName, String configNamespace, String
                                                releaseKey) {
            this.offerTime = new Date();
            this.appId = appId;
            this.clusterName = clusterName;
            this.dataCenter = Strings.isNullOrEmpty(dataCenter) ? "" : dataCenter;
            this.ip = clientIp;
            this.configAppId = configAppId;
            this.configClusterName = configClusterName;
            this.configNamespace = configNamespace;
            this.releaseKey = releaseKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InstanceConfigAuditModel model = (InstanceConfigAuditModel) o;
            return Objects.equals(appId, model.appId) &&
                    Objects.equals(clusterName, model.clusterName) &&
                    Objects.equals(dataCenter, model.dataCenter) &&
                    Objects.equals(ip, model.ip) &&
                    Objects.equals(configAppId, model.configAppId) &&
                    Objects.equals(configClusterName, model.configClusterName) &&
                    Objects.equals(configNamespace, model.configNamespace) &&
                    Objects.equals(releaseKey, model.releaseKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(appId, clusterName, dataCenter, ip, configAppId, configClusterName,
                    configNamespace,
                    releaseKey);
        }
    }
}
