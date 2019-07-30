package lucky.apollo.core.service;


import lucky.apollo.common.entity.po.AppPO;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface AdminService {
    AppPO createNewApp(AppPO app);

    void deleteApp(AppPO app, String operator);
}
