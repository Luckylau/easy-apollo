package lucky.apollo.core.service.impl;

import com.google.gson.Gson;
import lucky.apollo.core.constant.OpAudit;
import lucky.apollo.core.entity.ReleaseHistoryPO;
import lucky.apollo.core.repository.ReleaseHistoryRepository;
import lucky.apollo.core.service.AuditService;
import lucky.apollo.core.service.ReleaseHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Service
public class ReleaseHistoryServiceImpl implements ReleaseHistoryService {

    private Gson gson = new Gson();

    @Autowired
    private ReleaseHistoryRepository releaseHistoryRepository;

    @Autowired
    private AuditService auditService;

    @Override
    public Page<ReleaseHistoryPO> findReleaseHistoriesByNamespace(String appId, String clusterName,
                                                                  String namespaceName, Pageable
                                                                          pageable) {
        return releaseHistoryRepository.findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(appId, clusterName,
                namespaceName, pageable);
    }

    @Override
    public Page<ReleaseHistoryPO> findByReleaseIdAndOperation(long releaseId, int operation, Pageable page) {
        return releaseHistoryRepository.findByReleaseIdAndOperationOrderByIdDesc(releaseId, operation, page);
    }

    @Override
    public Page<ReleaseHistoryPO> findByPreviousReleaseIdAndOperation(long previousReleaseId, int operation, Pageable page) {
        return releaseHistoryRepository.findByPreviousReleaseIdAndOperationOrderByIdDesc(previousReleaseId, operation, page);
    }

    @Override
    public Page<ReleaseHistoryPO> findByReleaseIdAndOperationInOrderByIdDesc(long releaseId, Set<Integer> operations, Pageable page) {
        return releaseHistoryRepository.findByReleaseIdAndOperationInOrderByIdDesc(releaseId, operations, page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReleaseHistoryPO createReleaseHistory(String appId, String clusterName, String
            namespaceName, String branchName, long releaseId, long previousReleaseId, int operation,
                                                 Map<String, Object> operationContext, String operator) {
        ReleaseHistoryPO releaseHistory = new ReleaseHistoryPO();
        releaseHistory.setAppId(appId);
        releaseHistory.setClusterName(clusterName);
        releaseHistory.setNamespaceName(namespaceName);
        releaseHistory.setBranchName(branchName);
        releaseHistory.setReleaseId(releaseId);
        releaseHistory.setPreviousReleaseId(previousReleaseId);
        releaseHistory.setOperation(operation);
        if (operationContext == null) {
            //default empty object
            releaseHistory.setOperationContext("{}");
        } else {
            releaseHistory.setOperationContext(gson.toJson(operationContext));
        }
        releaseHistory.setDataChangeCreatedTime(new Date());
        releaseHistory.setDataChangeCreatedBy(operator);
        releaseHistory.setDataChangeLastModifiedBy(operator);

        releaseHistoryRepository.save(releaseHistory);

        auditService.audit(ReleaseHistoryPO.class.getSimpleName(), releaseHistory.getId(),
                OpAudit.INSERT, releaseHistory.getDataChangeCreatedBy());

        return releaseHistory;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchDelete(String appId, String clusterName, String namespaceName, String operator) {
        return releaseHistoryRepository.batchDelete(appId, clusterName, namespaceName, operator);
    }
}
