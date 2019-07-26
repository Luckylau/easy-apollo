package lucky.apollo.portal.service.impl;

import com.google.common.collect.Lists;
import lucky.apollo.common.entity.dto.AppDTO;
import lucky.apollo.common.entity.dto.PageDTO;
import lucky.apollo.common.entity.po.AppPO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.portal.api.AdminServiceApi;
import lucky.apollo.portal.entity.bo.UserInfo;
import lucky.apollo.portal.repository.AppRepository;
import lucky.apollo.portal.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
@Service
public class AppServiceImpl implements AppService {

    private final AppRepository appRepository;

    private final UserService userService;

    private final AppNamespaceService appNamespaceService;

    private final RoleInitializationService roleInitializationService;

    private final FavoriteService favoriteService;

    private final RolePermissionService rolePermissionService;

    private final AdminServiceApi adminServiceApi;


    public AppServiceImpl(AppRepository appRepository, UserService userService, AppNamespaceService appNamespaceService, RoleInitializationService roleInitializationService, FavoriteService favoriteService, RolePermissionService rolePermissionService, AdminServiceApi adminServiceApi) {
        this.appRepository = appRepository;
        this.userService = userService;
        this.appNamespaceService = appNamespaceService;
        this.roleInitializationService = roleInitializationService;
        this.favoriteService = favoriteService;
        this.rolePermissionService = rolePermissionService;
        this.adminServiceApi = adminServiceApi;
    }

    @Override
    public List<AppPO> findAll() {
        Iterable<AppPO> apps = appRepository.findAll();
        return Lists.newArrayList((apps));
    }

    @Override
    public PageDTO<AppPO> findAll(Pageable pageable) {
        Page<AppPO> apps = appRepository.findAll(pageable);

        return new PageDTO<>(apps.getContent(), pageable, apps.getTotalElements());
    }

    @Override
    public PageDTO<AppPO> searchByAppIdOrAppName(String query, Pageable pageable) {
        Page<AppPO> apps = appRepository.findByAppIdContainingOrNameContaining(query, query, pageable);

        return new PageDTO<>(apps.getContent(), pageable, apps.getTotalElements());
    }

    @Override
    public List<AppPO> findByAppIds(Set<String> appIds) {
        return appRepository.findByAppIdIn(appIds);
    }

    @Override
    public List<AppPO> findByAppIds(Set<String> appIds, Pageable pageable) {
        return appRepository.findByAppIdIn(appIds, pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPO createAppInLocal(AppPO app) {
        String appId = app.getAppId();
        AppPO managedApp = appRepository.findByAppId(appId);

        if (managedApp != null) {
            throw new BadRequestException(String.format("App already exists. AppId = %s", appId));
        }

        UserInfo owner = userService.findByusername(app.getOwnerName());
        if (owner == null) {
            throw new BadRequestException("Application's owner not exist.");
        }
        app.setOwnerEmail(owner.getEmail());

        String operator = userService.getCurrentUser().getUserId();
        app.setDataChangeCreatedBy(operator);
        app.setDataChangeLastModifiedBy(operator);

        AppPO createdApp = appRepository.save(app);

        appNamespaceService.createDefaultAppNamespace(appId);
        roleInitializationService.initAppRoles(createdApp);

        return createdApp;
    }

    @Override
    public void createAppInRemote(AppPO app) {
        String username = userService.getCurrentUser().getUserId();
        app.setDataChangeCreatedBy(username);
        app.setDataChangeLastModifiedBy(username);

        AppDTO appDTO = BeanUtils.transformWithIgnoreNull(AppDTO.class, app);
        adminServiceApi.createApp(appDTO);
    }

    @Override
    @Transactional
    public AppPO updateAppInLocal(AppPO app) {
        String appId = app.getAppId();

        AppPO managedApp = appRepository.findByAppId(appId);
        if (managedApp == null) {
            throw new BadRequestException(String.format("App not exists. AppId = %s", appId));
        }

        managedApp.setName(app.getName());
        managedApp.setOrgId(app.getOrgId());
        managedApp.setOrgName(app.getOrgName());

        String ownerName = app.getOwnerName();
        UserInfo owner = userService.findByusername(ownerName);
        if (owner == null) {
            throw new BadRequestException(String.format("App's owner not exists. owner = %s", ownerName));
        }
        managedApp.setOwnerName(owner.getUserId());
        managedApp.setOwnerEmail(owner.getEmail());

        String operator = userService.getCurrentUser().getUserId();
        managedApp.setDataChangeLastModifiedBy(operator);

        return appRepository.save(managedApp);
    }

    @Override
    public AppPO load(String appId) {
        return appRepository.findByAppId(appId);
    }


    @Override
    @Transactional
    public AppPO deleteAppInLocal(String appId) {
        AppPO managedApp = appRepository.findByAppId(appId);
        if (managedApp == null) {
            throw new BadRequestException(String.format("App not exists. AppId = %s", appId));
        }
        String operator = userService.getCurrentUser().getUserId();

        //this operator is passed to com.ctrip.framework.apollo.portal.listener.DeletionListener.onAppDeletionEvent
        managedApp.setDataChangeLastModifiedBy(operator);

        //删除portal数据库中的app
        appRepository.deleteApp(appId, operator);

        //删除portal数据库中的appNamespace
        appNamespaceService.batchDeleteByAppId(appId, operator);

        //删除portal数据库中的收藏表
        favoriteService.batchDeleteByAppId(appId, operator);

        //删除portal数据库中Permission、Role相关数据
        rolePermissionService.deleteRolePermissionsByAppId(appId, operator);

        return managedApp;
    }
}