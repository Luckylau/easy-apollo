package lucky.apollo.portal.repository;


import lucky.apollo.portal.entity.po.UserPO;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
public interface UserRepository extends PagingAndSortingRepository<UserPO, Long> {
    UserPO findByUsername(String username);

    List<UserPO> findFirst20ByEnabled(int enabled);

    List<UserPO> findByUsernameIn(List<String> userNames);

    List<UserPO> findByUsernameLikeAndEnabled(String username, int enabled);

}