package lucky.apollo.core.repository;

import lucky.apollo.core.entity.ItemPO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface ItemRepository extends PagingAndSortingRepository<ItemPO, Long> {

    ItemPO findByNamespaceIdAndKey(Long namespaceId, String key);

    List<ItemPO> findByNamespaceIdOrderByLineNumAsc(Long namespaceId);

    List<ItemPO> findByNamespaceId(Long namespaceId);

    List<ItemPO> findByNamespaceIdAndDataChangeLastModifiedTimeGreaterThan(Long namespaceId, Date date);

    ItemPO findFirst1ByNamespaceIdOrderByLineNumDesc(Long namespaceId);

    @Modifying
    @Query(value = "update Item set isdeleted=1,DataChange_LastModifiedBy = ?2 where namespaceId = ?1", nativeQuery = true)
    int deleteByNamespaceId(long namespaceId, String operator);

}
