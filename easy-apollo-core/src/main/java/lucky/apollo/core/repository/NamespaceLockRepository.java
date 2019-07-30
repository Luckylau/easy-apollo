package lucky.apollo.core.repository;

import lucky.apollo.core.entity.NamespaceLockPO;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
public interface NamespaceLockRepository extends PagingAndSortingRepository<NamespaceLockPO, Long> {

    NamespaceLockPO findByNamespaceId(Long namespaceId);

    Long deleteByNamespaceId(Long namespaceId);

}
