package lucky.apollo.metaservice.controller;

import com.netflix.appinfo.InstanceInfo;
import lucky.apollo.metaservice.entity.ServiceDTO;
import lucky.apollo.metaservice.service.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author luckylau
 * @Date 2019/9/17
 */
@RestController
@RequestMapping("/services")
public class ServiceController {
    @Autowired
    private DiscoveryService discoveryService;

    @RequestMapping("/meta")
    public List<ServiceDTO> getMetaService() {
        List<InstanceInfo> instances = discoveryService.getMetaServiceInstances();
        return transfer(instances);
    }

    @RequestMapping()
    public Map<String, List<InstanceInfo>> getServices() {
        return discoveryService.getServiceInstances();
    }

    @RequestMapping("/config")
    public List<ServiceDTO> getConfigService(
            @RequestParam(value = "appId", defaultValue = "") String appId,
            @RequestParam(value = "ip", required = false) String clientIp) {
        List<InstanceInfo> instances = discoveryService.getConfigServiceInstances();
        return transfer(instances);
    }

    @RequestMapping("/admin")
    public List<ServiceDTO> getAdminService() {
        List<InstanceInfo> instances = discoveryService.getAdminServiceInstances();
        return transfer(instances);
    }

    private List<ServiceDTO> transfer(List<InstanceInfo> instances) {
        if (instances == null || instances.isEmpty()) {
            return new ArrayList<>();
        }
        return instances.stream().map(instance -> {
            ServiceDTO service = new ServiceDTO();
            service.setAppName(instance.getAppName());
            service.setInstanceId(instance.getInstanceId());
            service.setHomepageUrl(instance.getHomePageUrl());
            return service;
        }).collect(Collectors.toList());
    }
}
