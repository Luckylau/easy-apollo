package lucky.apollo.service;

import lucky.apollo.entity.dto.PageDTO;
import lucky.apollo.entity.po.AppPO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
public interface AppService {
    List<AppPO> findAll();

    PageDTO<AppPO> findAll(Pageable pageable);

    PageDTO<AppPO> searchByAppIdOrAppName(String query, Pageable pageable);

    List<AppPO> findByAppIds(Set<String> appIds);

    List<AppPO> findByAppIds(Set<String> appIds, Pageable pageable);

    AppPO createAppInLocal(AppPO app);

    AppPO updateAppInLocal(AppPO appPO);

    void createAppInRemote(AppPO app);

    AppPO deleteAppInLocal(String appId);

    AppPO load(String appId);
}