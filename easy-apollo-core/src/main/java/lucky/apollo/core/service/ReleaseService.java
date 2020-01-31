package lucky.apollo.core.service;

import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.entity.ReleasePO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface ReleaseService {
    ReleasePO findOne(long releaseId);

    ReleasePO findActiveOne(long releaseId);

    List<ReleasePO> findByReleaseIds(Set<Long> releaseIds);

    List<ReleasePO> findByReleaseKeys(Set<String> releaseKeys);

    ReleasePO findLatestActiveRelease(String appId, String clusterName, String namespaceName);

    ReleasePO findLatestActiveRelease(NamespacePO namespace);

    List<ReleasePO> findAllReleases(String appId, String clusterName, String namespaceName, Pageable page);

    List<ReleasePO> findActiveReleases(String appId, String clusterName, String namespaceName, Pageable page);

    ReleasePO mergeBranchChangeSetsAndRelease(NamespacePO namespace, String branchName, String releaseName,
                                              String releaseComment, boolean isEmergencyPublish,
                                              ItemChangeSetsDTO changeSets);

    ReleasePO publish(NamespacePO namespace, String releaseName, String releaseComment,
                      String operator, boolean isEmergencyPublish);

    ReleasePO grayDeletionPublish(NamespacePO namespace, String releaseName, String releaseComment,
                                  String operator, boolean isEmergencyPublish, Set<String> grayDelKeys);

    int batchDelete(String appId, String clusterName, String namespaceName, String operator);

    ReleasePO rollback(long releaseId, String operator);
}
