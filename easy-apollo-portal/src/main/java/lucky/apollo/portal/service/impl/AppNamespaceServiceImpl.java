package lucky.apollo.portal.service.impl;


import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.constant.ConfigFileFormat;
import lucky.apollo.common.entity.po.AppNamespacePO;
import lucky.apollo.portal.repository.AppNamespaceRepository;
import lucky.apollo.portal.service.AppNamespaceService;
import lucky.apollo.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
@Service
public class AppNamespaceServiceImpl implements AppNamespaceService {

    @Autowired
    private AppNamespaceRepository appNamespaceRepository;

    @Autowired
    private UserService userService;


    @Override
    public void createDefaultAppNamespace(String appId) {
        AppNamespacePO appNs = new AppNamespacePO();
        appNs.setAppId(appId);
        appNs.setName(ConfigConsts.NAMESPACE_APPLICATION);
        appNs.setComment("default app namespace");
        appNs.setFormat(ConfigFileFormat.Properties.getValue());

        String operator = userService.getCurrentUser().getUserId();
        appNs.setDataChangeCreatedBy(operator);
        appNs.setDataChangeLastModifiedBy(operator);

        appNamespaceRepository.save(appNs);
    }

    @Override
    public void batchDeleteByAppId(String appId, String operator) {

    }

    @Override
    public AppNamespacePO findByAppIdAndName(String appId, String namespaceName) {
        return null;
    }

    @Override
    public AppNamespacePO deleteAppNamespace(String appId, String namespaceName) {
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppNamespacePO createAppNamespaceInLocal(AppNamespacePO appNamespace, boolean appendNamespacePrefix) {
        return null;
    }

    @Override
    public List<AppNamespacePO> findByAppId(String appId) {
        return null;
    }


}