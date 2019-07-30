package lucky.apollo.core.service;

import lucky.apollo.core.constant.OpAudit;
import lucky.apollo.core.entity.AuditPO;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface AuditService {

    List<AuditPO> find(String owner, String entity, String op);

    List<AuditPO> findByOwner(String owner);

    void audit(String entityName, Long entityId, OpAudit opAudit, String owner);

    void audit(AuditPO audit);

}
