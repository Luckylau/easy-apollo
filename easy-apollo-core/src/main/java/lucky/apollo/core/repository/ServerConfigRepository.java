package lucky.apollo.core.repository;

import lucky.apollo.core.entity.ServerConfigPO;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @Author luckylau
 * @Date 2019/9/18
 */
public interface ServerConfigRepository extends PagingAndSortingRepository<ServerConfigPO, Long> {

}
