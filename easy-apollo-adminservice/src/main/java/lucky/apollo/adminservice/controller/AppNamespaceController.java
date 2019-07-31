package lucky.apollo.adminservice.controller;


import lucky.apollo.common.constant.ConfigFileFormat;
import lucky.apollo.common.entity.dto.AppNamespaceDTO;
import lucky.apollo.common.entity.po.AppNamespacePO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.common.utils.StringUtils;
import lucky.apollo.core.service.AppNamespaceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@RestController
public class AppNamespaceController {

    private final AppNamespaceService appNamespaceService;

    public AppNamespaceController(final AppNamespaceService appNamespaceService) {
        this.appNamespaceService = appNamespaceService;
    }

    @PostMapping("/apps/{appId}/appnamespaces")
    public AppNamespaceDTO create(@RequestBody AppNamespaceDTO appNamespace,
                                  @RequestParam(defaultValue = "false") boolean silentCreation) {

        AppNamespacePO entity = BeanUtils.transformWithIgnoreNull(AppNamespacePO.class, appNamespace);
        AppNamespacePO managedEntity = appNamespaceService.findOne(entity.getAppId(), entity.getName());

        if (managedEntity == null) {
            if (StringUtils.isEmpty(entity.getFormat())) {
                entity.setFormat(ConfigFileFormat.Properties.getValue());
            }

            entity = appNamespaceService.createAppNamespace(entity);
        } else {
            throw new BadRequestException("app namespaces already exist.");
        }

        return BeanUtils.transformWithIgnoreNull(AppNamespaceDTO.class, entity);
    }

    @DeleteMapping("/apps/{appId}/appnamespaces/{namespaceName:.+}")
    public void delete(@PathVariable("appId") String appId, @PathVariable("namespaceName") String namespaceName,
                       @RequestParam String operator) {
        AppNamespacePO entity = appNamespaceService.findOne(appId, namespaceName);
        if (entity == null) {
            throw new BadRequestException("app namespace not found for appId: " + appId + " namespace: " + namespaceName);
        }
        appNamespaceService.deleteAppNamespace(entity, operator);
    }

    @GetMapping("/apps/{appId}/appnamespaces")
    public List<AppNamespaceDTO> getAppNamespaces(@PathVariable("appId") String appId) {

        List<AppNamespacePO> appNamespaces = appNamespaceService.findByAppId(appId);

        return BeanUtils.batchTransformWithIgnoreNull(AppNamespaceDTO.class, appNamespaces);
    }
}