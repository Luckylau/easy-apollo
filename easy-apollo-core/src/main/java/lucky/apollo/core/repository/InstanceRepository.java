package lucky.apollo.core.repository;

import lucky.apollo.core.entity.InstancePO;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
public interface InstanceRepository extends PagingAndSortingRepository<InstancePO, Long> {
    InstancePO findByAppIdAndClusterNameAndDataCenterAndIp(String appId, String clusterName, String dataCenter, String ip);
}
