package lucky.apollo.adminservice.controller;

import lucky.apollo.common.entity.dto.NamespaceLockDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.config.ServiceConfig;
import lucky.apollo.core.entity.NamespaceLockPO;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.service.NamespaceLockService;
import lucky.apollo.core.service.NamespaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@RestController
public class NamespaceLockController {

    private final NamespaceLockService namespaceLockService;
    private final NamespaceService namespaceService;
    private final ServiceConfig serviceConfig;

    public NamespaceLockController(
            final NamespaceLockService namespaceLockService,
            final NamespaceService namespaceService,
            final ServiceConfig serviceConfig) {
        this.namespaceLockService = namespaceLockService;
        this.namespaceService = namespaceService;
        this.serviceConfig = serviceConfig;
    }

    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/lock")
    public NamespaceLockDTO getNamespaceLockOwner(@PathVariable String appId, @PathVariable String clusterName,
                                                  @PathVariable String namespaceName) {
        NamespacePO namespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (namespace == null) {
            throw new BadRequestException("namespace not exist.");
        }


        NamespaceLockPO lock = namespaceLockService.findLock(namespace.getId());

        if (lock == null) {
            return null;
        }

        return BeanUtils.transformWithIgnoreNull(NamespaceLockDTO.class, lock);
    }
}