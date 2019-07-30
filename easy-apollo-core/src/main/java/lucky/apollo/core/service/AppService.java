package lucky.apollo.core.service;

import lucky.apollo.common.entity.po.AppPO;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface AppService {

    AppPO findOne(String appId);

    boolean isAppIdUnique(String appId);

    void delete(long id, String operator);

    List<AppPO> findAll(Pageable pageable);

    List<AppPO> findByName(String name);

    AppPO save(AppPO entity);

    void update(AppPO app);
}
