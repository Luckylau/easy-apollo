package lucky.apollo.core.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.constant.ConfigFileFormat;
import lucky.apollo.common.entity.po.AppNamespacePO;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.common.utils.StringUtils;
import lucky.apollo.core.constant.OpAudit;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.repository.AppNamespaceRepository;
import lucky.apollo.core.service.AppNamespaceService;
import lucky.apollo.core.service.AuditService;
import lucky.apollo.core.service.NamespaceService;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Service
@Slf4j
public class AppNamespaceServiceImpl implements AppNamespaceService {

    @Autowired
    private AppNamespaceRepository appNamespaceRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private NamespaceService namespaceService;


    @Override
    public boolean isAppNamespaceNameUnique(String appId, String namespaceName) {
        Objects.requireNonNull(appId, "AppId must not be null");
        Objects.requireNonNull(namespaceName, "Namespace must not be null");
        return Objects.isNull(appNamespaceRepository.findByAppIdAndName(appId, namespaceName));
    }

    @Override
    public AppNamespacePO findPublicNamespaceByName(String namespaceName) {
        Preconditions.checkArgument(namespaceName != null, "Namespace must not be null");
        return null;
    }

    @Override
    public List<AppNamespacePO> findByAppId(String appId) {
        return appNamespaceRepository.findByAppId(appId);
    }

    @Override
    public List<AppNamespacePO> findPublicNamespacesByNames(Set<String> namespaceNames) {
        if (namespaceNames == null || namespaceNames.isEmpty()) {
            return Collections.emptyList();
        }

        return null;
    }

    @Override
    public List<AppNamespacePO> findPrivateAppNamespace(String appId) {
        return null;
    }

    @Override
    public AppNamespacePO findOne(String appId, String namespaceName) {
        Preconditions
                .checkArgument(!StringUtils.isContainEmpty(appId, namespaceName), "appId or Namespace must not be null");
        return appNamespaceRepository.findByAppIdAndName(appId, namespaceName);
    }

    @Override
    public List<AppNamespacePO> findByAppIdAndNamespaces(String appId, Set<String> namespaceNames) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(appId), "appId must not be null");
        if (namespaceNames == null || namespaceNames.isEmpty()) {
            return Collections.emptyList();
        }
        return appNamespaceRepository.findByAppIdAndNameIn(appId, namespaceNames);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createDefaultAppNamespace(String appId, String createBy) {
        if (!isAppNamespaceNameUnique(appId, ConfigConsts.NAMESPACE_APPLICATION)) {
            throw new ServiceException("appnamespace not unique");
        }
        AppNamespacePO appNs = new AppNamespacePO();
        appNs.setAppId(appId);
        appNs.setName(ConfigConsts.NAMESPACE_APPLICATION);
        appNs.setComment("default app namespace");
        appNs.setFormat(ConfigFileFormat.Properties.getValue());
        appNs.setDataChangeCreatedBy(createBy);
        appNs.setDataChangeLastModifiedBy(createBy);
        appNamespaceRepository.save(appNs);

        auditService.audit(AppNamespacePO.class.getSimpleName(), appNs.getId(), OpAudit.INSERT,
                createBy);
    }

    @Transactional
    @Override
    public AppNamespacePO createAppNamespace(AppNamespacePO appNamespace) {
        String createBy = appNamespace.getDataChangeCreatedBy();
        if (!isAppNamespaceNameUnique(appNamespace.getAppId(), appNamespace.getName())) {
            throw new ServiceException("appnamespace not unique");
        }
        //protection
        appNamespace.setId(0);
        appNamespace.setDataChangeCreatedBy(createBy);
        appNamespace.setDataChangeLastModifiedBy(createBy);

        appNamespace = appNamespaceRepository.save(appNamespace);


        auditService.audit(AppNamespacePO.class.getSimpleName(), appNamespace.getId(), OpAudit.INSERT, createBy);
        return appNamespace;
    }

    @Override
    public AppNamespacePO update(AppNamespacePO appNamespace) {
        AppNamespacePO managedNs = appNamespaceRepository.findByAppIdAndName(appNamespace.getAppId(), appNamespace.getName());
        BeanUtils.copyPropertiesWithIgnore(appNamespace, managedNs);
        managedNs = appNamespaceRepository.save(managedNs);

        auditService.audit(AppNamespacePO.class.getSimpleName(), managedNs.getId(), OpAudit.UPDATE,
                managedNs.getDataChangeLastModifiedBy());

        return managedNs;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDelete(String appId, String operator) {
        appNamespaceRepository.batchDeleteByAppId(appId, operator);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAppNamespace(AppNamespacePO appNamespace, String operator) {
        String appId = appNamespace.getAppId();
        String namespaceName = appNamespace.getName();

        log.info("{} is deleting AppNamespace, appId: {}, namespace: {}", operator, appId, namespaceName);

        // 1. delete namespaces
        List<NamespacePO> namespaces = namespaceService.findByAppIdAndNamespaceName(appId, namespaceName);

        if (namespaces != null) {
            for (NamespacePO namespace : namespaces) {
                namespaceService.deleteNamespace(namespace, operator);
            }
        }

        // 2. delete app namespace
        appNamespaceRepository.delete(appId, namespaceName, operator);
    }
}
