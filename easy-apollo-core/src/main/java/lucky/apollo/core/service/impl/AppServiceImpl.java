package lucky.apollo.core.service.impl;

import lucky.apollo.common.entity.po.AppPO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.core.constant.OpAudit;
import lucky.apollo.core.repository.AppRepository;
import lucky.apollo.core.service.AppService;
import lucky.apollo.core.service.AuditService;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Service
public class AppServiceImpl implements AppService {

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AuditService auditService;

    @Override
    public boolean isAppIdUnique(String appId) {
        Objects.requireNonNull(appId, "AppId must not be null");
        return Objects.isNull(appRepository.findByAppId(appId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(long id, String operator) {
        AppPO app = appRepository.findById(id).orElse(null);
        if (app == null) {
            return;
        }

        app.setDeleted(true);
        app.setDataChangeLastModifiedBy(operator);
        appRepository.save(app);

        auditService.audit(AppPO.class.getSimpleName(), id, OpAudit.DELETE, operator);
    }

    @Override
    public List<AppPO> findAll(Pageable pageable) {
        Page<AppPO> page = appRepository.findAll(pageable);
        return page.getContent();
    }

    @Override
    public List<AppPO> findByName(String name) {
        return appRepository.findByName(name);
    }

    @Override
    public AppPO findOne(String appId) {
        return appRepository.findByAppId(appId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppPO save(AppPO entity) {
        if (!isAppIdUnique(entity.getAppId())) {
            throw new ServiceException("appId not unique");
        }
        entity.setId(0);
        AppPO app = appRepository.save(entity);

        auditService.audit(AppPO.class.getSimpleName(), app.getId(), OpAudit.INSERT,
                app.getDataChangeCreatedBy());

        return app;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(AppPO app) {
        String appId = app.getAppId();

        AppPO managedApp = appRepository.findByAppId(appId);
        if (managedApp == null) {
            throw new BadRequestException(String.format("App not exists. AppId = %s", appId));
        }

        managedApp.setName(app.getName());
        managedApp.setOrgId(app.getOrgId());
        managedApp.setOrgName(app.getOrgName());
        managedApp.setOwnerName(app.getOwnerName());
        managedApp.setOwnerEmail(app.getOwnerEmail());
        managedApp.setDataChangeLastModifiedBy(app.getDataChangeLastModifiedBy());

        managedApp = appRepository.save(managedApp);

        auditService.audit(AppPO.class.getSimpleName(), managedApp.getId(), OpAudit.UPDATE,
                managedApp.getDataChangeLastModifiedBy());

    }
}
