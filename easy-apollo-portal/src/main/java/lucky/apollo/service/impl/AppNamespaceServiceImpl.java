package lucky.apollo.service.impl;

import lucky.apollo.entity.po.AppNamespacePO;
import lucky.apollo.service.AppNamespaceService;
import org.springframework.stereotype.Service;

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

}