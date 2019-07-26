package lucky.apollo.portal.service.impl;


import lucky.apollo.common.entity.po.AppNamespacePO;
import lucky.apollo.portal.service.AppNamespaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
@Service
public class AppNamespaceServiceImpl implements AppNamespaceService {

    @Override
    public void createDefaultAppNamespace(String appId) {

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