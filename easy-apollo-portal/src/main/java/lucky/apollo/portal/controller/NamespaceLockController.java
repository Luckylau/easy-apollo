package lucky.apollo.portal.controller;


import lucky.apollo.common.entity.dto.NamespaceLockDTO;
import lucky.apollo.portal.adminsevice.api.AdminServiceApi;
import lucky.apollo.portal.entity.vo.Lock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@RestController
public class NamespaceLockController {

    private final AdminServiceApi adminServiceApi;

    public NamespaceLockController(AdminServiceApi adminServiceApi) {
        this.adminServiceApi = adminServiceApi;
    }

    @GetMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/lock-info")
    public Lock getNamespaceLockInfo(@PathVariable String appId, @PathVariable String env,
                                     @PathVariable String clusterName, @PathVariable String namespaceName) {

        Lock lock = new Lock();

        NamespaceLockDTO namespaceLockDTO = adminServiceApi.getNamespaceLockOwner(appId, namespaceName);
        String lockOwner = namespaceLockDTO == null ? "" : namespaceLockDTO.getDataChangeCreatedBy();
        lock.setLockOwner(lockOwner);

        lock.setEmergencyPublishAllowed(false);

        return lock;

    }

}


