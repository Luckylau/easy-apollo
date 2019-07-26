package lucky.apollo.portal.repository;


import lucky.apollo.portal.entity.po.ServerConfigPO;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @Author luckylau
 * @Date 2019/7/15
 */
public interface ServerConfigRepository extends PagingAndSortingRepository<ServerConfigPO, Long> {
    ServerConfigPO findByKey(String key);
}
