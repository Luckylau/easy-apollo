package lucky.apollo.core.repository;

import lucky.apollo.common.entity.po.AppPO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface AppRepository extends PagingAndSortingRepository<AppPO, Long> {

    @Query(value = "SELECT a from App a WHERE a.name LIKE %:name%", nativeQuery = true)
    List<AppPO> findByName(@Param("name") String name);

    AppPO findByAppId(String appId);
}

