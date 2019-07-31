package lucky.apollo.adminservice.controller;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */

import lucky.apollo.common.entity.dto.NamespaceDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.exception.NotFoundException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.service.NamespaceService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

public class NamespaceController {

    private final NamespaceService namespaceService;

    public NamespaceController(final NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces")
    public NamespaceDTO create(@PathVariable("appId") String appId,
                               @PathVariable("clusterName") String clusterName,
                               @Valid @RequestBody NamespaceDTO dto) {
        NamespacePO entity = BeanUtils.transformWithIgnoreNull(NamespacePO.class, dto);
        NamespacePO managedEntity = namespaceService.findOne(appId, clusterName, entity.getNamespaceName());
        if (managedEntity != null) {
            throw new BadRequestException("namespace already exist.");
        }

        entity = namespaceService.save(entity);

        return BeanUtils.transformWithIgnoreNull(NamespaceDTO.class, entity);
    }

    @DeleteMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName:.+}")
    public void delete(@PathVariable("appId") String appId,
                       @PathVariable("clusterName") String clusterName,
                       @PathVariable("namespaceName") String namespaceName, @RequestParam String operator) {
        NamespacePO entity = namespaceService.findOne(appId, clusterName, namespaceName);
        if (entity == null) throw new NotFoundException(
                String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));

        namespaceService.deleteNamespace(entity, operator);
    }


    @GetMapping("/namespaces/{namespaceId}")
    public NamespaceDTO get(@PathVariable("namespaceId") Long namespaceId) {
        NamespacePO namespace = namespaceService.findOne(namespaceId);
        if (namespace == null)
            throw new NotFoundException(String.format("namespace not found for %s", namespaceId));
        return BeanUtils.transformWithIgnoreNull(NamespaceDTO.class, namespace);
    }

    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName:.+}")
    public NamespaceDTO get(@PathVariable("appId") String appId,
                            @PathVariable("clusterName") String clusterName,
                            @PathVariable("namespaceName") String namespaceName) {
        NamespacePO namespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (namespace == null) throw new NotFoundException(
                String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
        return BeanUtils.transformWithIgnoreNull(NamespaceDTO.class, namespace);
    }
}