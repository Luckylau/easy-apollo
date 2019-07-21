package lucky.apollo.repository;

import lucky.apollo.entity.po.UserRolePO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/15
 */
public interface UserRoleRepository extends PagingAndSortingRepository<UserRolePO, Long> {
    /**
     * find user roles by userId
     */
    List<UserRolePO> findByUserId(String userId);

    /**
     * find user roles by roleId
     */
    List<UserRolePO> findByRoleId(long roleId);

    /**
     * find user roles by userIds and roleId
     */
    List<UserRolePO> findByUserIdInAndRoleId(Collection<String> userId, long roleId);

    /**
     * 批量删除
     *
     * @param roleIds
     * @param operator
     * @return
     */
    @Modifying
    @Query(value = "UPDATE UserRole SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE RoleId in ?1", nativeQuery = true)
    Integer batchDeleteByRoleIds(List<Long> roleIds, String operator);

}