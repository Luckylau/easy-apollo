package lucky.apollo.portal.controller;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.entity.dto.AppNamespaceDTO;
import lucky.apollo.common.entity.dto.NamespaceDTO;
import lucky.apollo.common.entity.po.AppNamespacePO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.common.utils.RequestPrecondition;
import lucky.apollo.portal.api.AdminServiceApi;
import lucky.apollo.portal.entity.bo.NamespaceInfo;
import lucky.apollo.portal.entity.model.NamespaceCreationModel;
import lucky.apollo.portal.listener.AppNamespaceCreationEvent;
import lucky.apollo.portal.listener.AppNamespaceDeletionEvent;
import lucky.apollo.portal.resolver.PermissionValidator;
import lucky.apollo.portal.service.AppNamespaceService;
import lucky.apollo.portal.service.NamespaceService;
import lucky.apollo.portal.service.RoleInitializationService;
import lucky.apollo.portal.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static lucky.apollo.common.utils.RequestPrecondition.checkModel;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@RestController
@Slf4j
public class NamespaceController {

    private final AppNamespaceService appNamespaceService;

    private final NamespaceService namespaceService;

    private final PermissionValidator permissionValidator;

    private final UserService userService;

    private final RoleInitializationService roleInitializationService;

    private final ApplicationEventPublisher publisher;

    private final AdminServiceApi adminServiceApi;


    public NamespaceController(AppNamespaceService appNamespaceService, NamespaceService namespaceService, PermissionValidator permissionValidator, UserService userService, RoleInitializationService roleInitializationService, ApplicationEventPublisher publisher, AdminServiceApi adminServiceApi) {
        this.appNamespaceService = appNamespaceService;
        this.namespaceService = namespaceService;
        this.permissionValidator = permissionValidator;
        this.userService = userService;
        this.roleInitializationService = roleInitializationService;
        this.publisher = publisher;
        this.adminServiceApi = adminServiceApi;
    }

    @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces")
    public List<NamespaceInfo> findNamespaces(@PathVariable String appId, @PathVariable String env,
                                              @PathVariable String clusterName) {

        List<NamespaceInfo> namespaceInfos = namespaceService.findNamespaceBOs(appId);

        for (NamespaceInfo namespaceInfo : namespaceInfos) {
            if (permissionValidator.shouldHideConfigToCurrentUser(appId, namespaceInfo.getBaseInfo().getNamespaceName())) {
                namespaceInfo.hideItems();
            }
        }

        return namespaceInfos;
    }

    @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName:.+}")
    public NamespaceInfo findNamespace(@PathVariable String appId, @PathVariable String env,
                                       @PathVariable String clusterName, @PathVariable String namespaceName) {

        NamespaceInfo namespaceInfo = namespaceService.loadNamespaceBO(appId, namespaceName);

        if (namespaceInfo != null && permissionValidator.shouldHideConfigToCurrentUser(appId, namespaceName)) {
            namespaceInfo.hideItems();
        }

        return namespaceInfo;
    }

    @PreAuthorize(value = "@permissionValidator.hasCreateNamespacePermission(#appId)")
    @PostMapping("/apps/{appId}/namespaces")
    public ResponseEntity<Void> createNamespace(@PathVariable String appId,
                                                @RequestBody List<NamespaceCreationModel> models) {

        checkModel(!CollectionUtils.isEmpty(models));

        String namespaceName = models.get(0).getNamespace().getNamespaceName();
        String operator = userService.getCurrentUser().getUserId();

        roleInitializationService.initNamespaceRoles(appId, namespaceName, operator);

        for (NamespaceCreationModel model : models) {
            NamespaceDTO namespace = model.getNamespace();
            RequestPrecondition.checkArgumentsNotEmpty(model.getEnv(), namespace.getAppId(),
                    namespace.getClusterName(), namespace.getNamespaceName());

            try {
                namespaceService.createNamespace(namespace);
            } catch (Exception e) {
                log.error("create namespace fail.", e);
            }
        }

        namespaceService.assignNamespaceRoleToOperator(appId, namespaceName, userService.getCurrentUser().getUserId());

        return ResponseEntity.ok().build();
    }

    @PreAuthorize(value = "@permissionValidator.hasDeleteNamespacePermission(#appId)")
    @DeleteMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName:.+}")
    public ResponseEntity<Void> deleteNamespace(@PathVariable String appId, @PathVariable String env,
                                                @PathVariable String clusterName, @PathVariable String namespaceName) {

        namespaceService.deleteNamespace(appId, namespaceName);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @DeleteMapping("/apps/{appId}/appnamespaces/{namespaceName:.+}")
    public ResponseEntity<Void> deleteAppNamespace(@PathVariable String appId, @PathVariable String namespaceName) {

        AppNamespacePO appNamespace = appNamespaceService.deleteAppNamespace(appId, namespaceName);

        publisher.publishEvent(new AppNamespaceDeletionEvent(appNamespace));

        return ResponseEntity.ok().build();
    }

    @GetMapping("/apps/{appId}/appnamespaces/{namespaceName:.+}")
    public AppNamespaceDTO findAppNamespace(@PathVariable String appId, @PathVariable String namespaceName) {
        AppNamespacePO appNamespace = appNamespaceService.findByAppIdAndName(appId, namespaceName);

        if (appNamespace == null) {
            throw new BadRequestException(
                    String.format("AppNamespace not exists. AppId = %s, NamespaceName = %s", appId, namespaceName));
        }

        return BeanUtils.transformWithIgnoreNull(AppNamespaceDTO.class, appNamespace);
    }

    @PreAuthorize(value = "@permissionValidator.hasCreateAppNamespacePermission(#appId, #appNamespace)")
    @PostMapping("/apps/{appId}/appnamespaces")
    public AppNamespacePO createAppNamespace(@PathVariable String appId,
                                             @RequestParam(defaultValue = "true") boolean appendNamespacePrefix,
                                             @Valid @RequestBody AppNamespacePO appNamespace) {
        AppNamespacePO createdAppNamespace = appNamespaceService.createAppNamespaceInLocal(appNamespace, appendNamespacePrefix);

        publisher.publishEvent(new AppNamespaceCreationEvent(createdAppNamespace));

        return createdAppNamespace;
    }

    @PostMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/missing-namespaces")
    public ResponseEntity<Void> createMissingNamespaces(@PathVariable String appId, @PathVariable String env, @PathVariable String clusterName) {

        Set<String> missingNamespaces = findMissingNamespaceNames(appId);

        for (String missingNamespace : missingNamespaces) {
            adminServiceApi.createMissingAppNamespace(findAppNamespace(appId, missingNamespace));
        }

        return ResponseEntity.ok().build();
    }

    private Set<String> findMissingNamespaceNames(String appId) {
        List<AppNamespaceDTO> configDbAppNamespaces = adminServiceApi.getAppNamespaces(appId);
        List<NamespaceDTO> configDbNamespaces = namespaceService.findNamespaces(appId);
        List<AppNamespacePO> portalDbAppNamespaces = appNamespaceService.findByAppId(appId);

        Set<String> configDbAppNamespaceNames = configDbAppNamespaces.stream().map(AppNamespaceDTO::getName)
                .collect(Collectors.toSet());
        Set<String> configDbNamespaceNames = configDbNamespaces.stream().map(NamespaceDTO::getNamespaceName)
                .collect(Collectors.toSet());

        Set<String> portalDbAllAppNamespaceNames = Sets.newHashSet();
        Set<String> portalDbPrivateAppNamespaceNames = Sets.newHashSet();

        for (AppNamespacePO appNamespace : portalDbAppNamespaces) {
            portalDbAllAppNamespaceNames.add(appNamespace.getName());
            if (!appNamespace.isPublic()) {
                portalDbPrivateAppNamespaceNames.add(appNamespace.getName());
            }
        }

        // AppNamespaces should be the same
        Set<String> missingAppNamespaceNames = Sets.difference(portalDbAllAppNamespaceNames, configDbAppNamespaceNames);
        // Private namespaces should all exist
        Set<String> missingNamespaceNames = Sets.difference(portalDbPrivateAppNamespaceNames, configDbNamespaceNames);

        return Sets.union(missingAppNamespaceNames, missingNamespaceNames);
    }
}