package lucky.apollo.portal.controller;

import com.google.common.collect.Sets;
import lucky.apollo.common.entity.dto.PageDTO;
import lucky.apollo.common.entity.po.AppPO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.portal.entity.model.AppModel;
import lucky.apollo.portal.entity.po.RolePO;
import lucky.apollo.portal.listener.AppChangedEvent;
import lucky.apollo.portal.listener.AppCreationEvent;
import lucky.apollo.portal.listener.AppDeletionEvent;
import lucky.apollo.portal.service.AppService;
import lucky.apollo.portal.service.RolePermissionService;
import lucky.apollo.portal.service.UserService;
import lucky.apollo.portal.utils.RoleUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
@RestController
@RequestMapping("/apps")
public class AppController {

    private final AppService appService;

    private final RolePermissionService rolePermissionService;

    private final UserService userService;

    private final ApplicationEventPublisher publisher;

    public AppController(AppService appService, RolePermissionService rolePermissionService, UserService userService, ApplicationEventPublisher publisher) {
        this.appService = appService;
        this.rolePermissionService = rolePermissionService;
        this.userService = userService;
        this.publisher = publisher;
    }

    @GetMapping
    public List<AppPO> findApps(@RequestParam(value = "appIds", required = false) String appIds) {
        if (StringUtils.isEmpty(appIds)) {
            return appService.findAll();
        } else {
            return appService.findByAppIds(Sets.newHashSet(appIds.split(",")));
        }
    }

    @GetMapping("/search")
    public PageDTO<AppPO> searchByAppIdOrAppName(@RequestParam(value = "query", required = false) String query,
                                                 Pageable pageable) {
        if (StringUtils.isEmpty(query)) {
            return appService.findAll(pageable);
        } else {
            return appService.searchByAppIdOrAppName(query, pageable);
        }
    }

    @GetMapping("/owner")
    public List<AppPO> findAppsByOwner(@RequestParam("owner") String owner, Pageable page) {
        Set<String> appIds = Sets.newHashSet();

        List<RolePO> userRoles = rolePermissionService.findUserRoles(owner);

        for (RolePO role : userRoles) {
            String appId = RoleUtils.extractAppIdFromRoleName(role.getRoleName());

            if (appId != null) {
                appIds.add(appId);
            }
        }

        return appService.findByAppIds(appIds, page);
    }

    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @PostMapping
    public AppPO create(@Valid @RequestBody AppModel appModel) {

        AppPO app = transformToApp(appModel);

        AppPO createdApp = appService.createAppInLocal(app);

        publisher.publishEvent(new AppCreationEvent(createdApp));

        Set<String> admins = appModel.getAdmins();
        if (!CollectionUtils.isEmpty(admins)) {
            rolePermissionService
                    .assignRoleToUsers(RoleUtils.buildAppMasterRoleName(createdApp.getAppId()),
                            admins, userService.getCurrentUser().getUserId());
        }

        return createdApp;
    }

    @PreAuthorize(value = "@permissionValidator.isAppAdmin(#appId)")
    @PutMapping("/{appId:.+}")
    public void update(@PathVariable String appId, @Valid @RequestBody AppModel appModel) {
        if (!Objects.equals(appId, appModel.getAppId())) {
            throw new BadRequestException("The App Id of path variable and request body is different");
        }

        AppPO app = transformToApp(appModel);

        AppPO updatedApp = appService.updateAppInLocal(app);

        publisher.publishEvent(new AppChangedEvent(updatedApp));
    }

    @GetMapping("/{appId:.+}")
    public AppPO load(@PathVariable String appId) {

        return appService.load(appId);
    }


    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @DeleteMapping("/{appId:.+}")
    public void deleteApp(@PathVariable String appId) {
        AppPO app = appService.deleteAppInLocal(appId);

        publisher.publishEvent(new AppDeletionEvent(app));
    }

    private AppPO transformToApp(AppModel appModel) {
        String appId = appModel.getAppId();
        String appName = appModel.getName();
        String ownerName = appModel.getOwnerName();
        String orgId = appModel.getOrgId();
        String orgName = appModel.getOrgName();

        return AppPO.builder()
                .appId(appId)
                .name(appName)
                .ownerName(ownerName)
                .orgId(orgId)
                .orgName(orgName)
                .build();

    }
}