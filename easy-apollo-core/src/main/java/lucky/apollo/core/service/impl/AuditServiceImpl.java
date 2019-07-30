package lucky.apollo.core.service.impl;

import lucky.apollo.core.constant.OpAudit;
import lucky.apollo.core.entity.AuditPO;
import lucky.apollo.core.repository.AuditRepository;
import lucky.apollo.core.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditRepository auditRepository;

    @Override
    public List<AuditPO> findByOwner(String owner) {
        return auditRepository.findByOwner(owner);
    }

    @Override
    public List<AuditPO> find(String owner, String entity, String op) {
        return auditRepository.findAudits(owner, entity, op);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(String entityName, Long entityId, OpAudit opAudit, String owner) {
        AuditPO audit = new AuditPO();
        audit.setEntityName(entityName);
        audit.setEntityId(entityId);
        audit.setOpName(opAudit.name());
        audit.setDataChangeCreatedBy(owner);
        auditRepository.save(audit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(AuditPO audit) {
        auditRepository.save(audit);
    }
}
