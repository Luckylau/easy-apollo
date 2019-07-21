package lucky.apollo.repository;

import lucky.apollo.entity.po.ConsumerRolePO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
public interface ConsumerRoleRepository extends PagingAndSortingRepository<ConsumerRolePO, Long> {
    /**
     * find consumer roles by userId
     *
     * @param consumerId consumer id
     */
    List<ConsumerRolePO> findByConsumerId(long consumerId);

    /**
     * find consumer roles by roleId
     */
    List<ConsumerRolePO> findByRoleId(long roleId);

    ConsumerRolePO findByConsumerIdAndRoleId(long consumerId, long roleId);

    @Modifying
    @Query(value = "UPDATE ConsumerRole SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE RoleId in ?1", nativeQuery = true)
    Integer batchDeleteByRoleIds(List<Long> roleIds, String operator);
}