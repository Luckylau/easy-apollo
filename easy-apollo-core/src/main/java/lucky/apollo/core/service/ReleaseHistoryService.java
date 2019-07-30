package lucky.apollo.core.service;

import lucky.apollo.core.entity.ReleaseHistoryPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface ReleaseHistoryService {

    Page<ReleaseHistoryPO> findReleaseHistoriesByNamespace(String appId, String clusterName,
                                                           String namespaceName, Pageable
                                                                   pageable);

    Page<ReleaseHistoryPO> findByReleaseIdAndOperation(long releaseId, int operation, Pageable page);

    Page<ReleaseHistoryPO> findByPreviousReleaseIdAndOperation(long previousReleaseId, int operation, Pageable page);

    Page<ReleaseHistoryPO> findByReleaseIdAndOperationInOrderByIdDesc(long releaseId, Set<Integer> operations, Pageable page);

    ReleaseHistoryPO createReleaseHistory(String appId, String clusterName, String
            namespaceName, String branchName, long releaseId, long previousReleaseId, int operation,
                                          Map<String, Object> operationContext, String operator);

    int batchDelete(String appId, String clusterName, String namespaceName, String operator);

}
