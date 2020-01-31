package lucky.apollo.core.repository;

import lucky.apollo.core.entity.GrayReleaseRulePO;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/9/23
 */
public interface GrayReleaseRuleRepository extends PagingAndSortingRepository<GrayReleaseRulePO, Long> {

    GrayReleaseRulePO findTopByAppIdAndClusterNameAndNamespaceNameAndBranchNameOrderByIdDesc(String appId, String clusterName,
                                                                                             String namespaceName, String branchName);

    List<GrayReleaseRulePO> findByAppIdAndClusterNameAndNamespaceName(String appId,
                                                                      String clusterName, String namespaceName);

    List<GrayReleaseRulePO> findFirst500ByIdGreaterThanOrderByIdAsc(Long id);
}
