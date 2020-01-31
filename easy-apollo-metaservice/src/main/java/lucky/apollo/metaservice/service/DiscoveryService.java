package lucky.apollo.metaservice.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.metaservice.constant.ServiceNameConsts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/9/17
 */
@Service
@Slf4j
public class DiscoveryService {

    @Qualifier("eurekaClient")
    @Autowired
    private EurekaClient eurekaClient;

    public Map<String, List<InstanceInfo>> getServiceInstances() {
        Map<String, List<InstanceInfo>> instanceInfoList = new HashMap<>();
        Applications applications = eurekaClient.getApplications();
        List<Application> list = applications.getRegisteredApplications();
        for (Application application : list) {
            instanceInfoList.put(application.getName(), application.getInstances());
        }

        return instanceInfoList;
    }


    public List<InstanceInfo> getConfigServiceInstances() {
        Application application = eurekaClient.getApplication(ServiceNameConsts.APOLLO_CONFIGSERVICE);
        if (application == null) {
            log.warn("Apollo.EurekaDiscovery.NotFound, {}", ServiceNameConsts.APOLLO_CONFIGSERVICE);
        }
        return application != null ? application.getInstances() : Collections.emptyList();
    }

    public List<InstanceInfo> getMetaServiceInstances() {
        Application application = eurekaClient.getApplication(ServiceNameConsts.APOLLO_METASERVICE);
        if (application == null) {
            log.warn("Apollo.EurekaDiscovery.NotFound, {}", ServiceNameConsts.APOLLO_METASERVICE);
        }
        return application != null ? application.getInstances() : Collections.emptyList();
    }

    public List<InstanceInfo> getAdminServiceInstances() {
        Application application = eurekaClient.getApplication(ServiceNameConsts.APOLLO_ADMINSERVICE);
        if (application == null) {
            log.warn("Apollo.EurekaDiscovery.NotFound, {}", ServiceNameConsts.APOLLO_ADMINSERVICE);
        }
        return application != null ? application.getInstances() : Collections.emptyList();
    }
}
