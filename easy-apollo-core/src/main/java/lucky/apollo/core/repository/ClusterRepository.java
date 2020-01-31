package lucky.apollo.core.repository;

import lucky.apollo.core.entity.ClusterPO;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/10/11
 */
public interface ClusterRepository extends PagingAndSortingRepository<ClusterPO, Long> {

    List<ClusterPO> findByAppIdAndParentClusterId(String appId, Long parentClusterId);

    List<ClusterPO> findByAppId(String appId);

    ClusterPO findByAppIdAndName(String appId, String name);

    List<ClusterPO> findByParentClusterId(Long parentClusterId);
}

