package lucky.apollo.core.repository;

import lucky.apollo.core.entity.AuditPO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface AuditRepository extends PagingAndSortingRepository<AuditPO, Long> {

    @Query(value = "SELECT a from Audit a WHERE a.dataChangeCreatedBy = :owner", nativeQuery = true)
    List<AuditPO> findByOwner(@Param("owner") String owner);

    @Query(value = "SELECT a from Audit a WHERE a.dataChangeCreatedBy = :owner AND a.entityName =:entity AND a.opName = :op", nativeQuery = true)
    List<AuditPO> findAudits(@Param("owner") String owner, @Param("entity") String entity,
                             @Param("op") String op);
}