package lucky.apollo.core.repository;

import lucky.apollo.core.entity.ReleaseMessagePO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
public interface ReleaseMessageRepository extends PagingAndSortingRepository<ReleaseMessagePO, Long> {
    List<ReleaseMessagePO> findFirst500ByIdGreaterThanOrderByIdAsc(Long id);

    ReleaseMessagePO findTopByOrderByIdDesc();

    ReleaseMessagePO findTopByMessageInOrderByIdDesc(Collection<String> messages);

    List<ReleaseMessagePO> findFirst100ByMessageAndIdLessThanOrderByIdAsc(String message, Long id);

    @Query(value = "select message, max(id) as id from ReleaseMessage where message in :messages group by message", nativeQuery = true)
    List<Object[]> findLatestReleaseMessagesGroupByMessages(@Param("messages") Collection<String> messages);
}
