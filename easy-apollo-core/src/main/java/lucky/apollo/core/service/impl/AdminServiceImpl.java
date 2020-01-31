package lucky.apollo.core.service.impl;

import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.entity.po.AppPO;
import lucky.apollo.core.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AppService appService;

    @Autowired
    private AppNamespaceService appNamespaceService;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private ClusterService clusterService;

    @Override
    public AppPO createNewApp(AppPO app) {
        String createBy = app.getDataChangeCreatedBy();
        AppPO createdApp = appService.save(app);

        String appId = createdApp.getAppId();

        appNamespaceService.createDefaultAppNamespace(appId, createBy);

        clusterService.createDefaultCluster(appId, createBy);

        namespaceService.instanceOfAppNamespaces(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, createBy);

        return app;
    }

    @Override
    public void deleteApp(AppPO app, String operator) {

    }
}
