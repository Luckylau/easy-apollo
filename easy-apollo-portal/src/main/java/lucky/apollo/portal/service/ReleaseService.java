package lucky.apollo.portal.service;

import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ReleaseDTO;
import lucky.apollo.portal.entity.bo.ReleaseBO;
import lucky.apollo.portal.entity.model.NamespaceGrayDelReleaseModel;
import lucky.apollo.portal.entity.model.NamespaceReleaseModel;
import lucky.apollo.portal.entity.vo.ReleaseCompareResult;

import java.util.List;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/9/23
 */
public interface ReleaseService {
    ReleaseDTO publish(NamespaceReleaseModel model);

    ReleaseDTO publish(NamespaceGrayDelReleaseModel model, String releaseBy);

    List<ReleaseBO> findAllReleases(String appId, String namespaceName, String cluster, int page,
                                    int size);

    List<ReleaseDTO> findActiveReleases(String appId, String namespaceName, String cluster, int page,
                                        int size);

    List<ReleaseDTO> findReleaseByIds(Set<Long> releaseIds);

    ReleaseDTO loadLatestRelease(String appId, String namespaceName, String cluster);

    void rollback(long releaseId);

    ReleaseCompareResult compare(long baseReleaseId, long toCompareReleaseId);

    ReleaseCompareResult compare(ReleaseDTO baseRelease, ReleaseDTO toCompareRelease);

    ReleaseDTO updateAndPublish(String appId, String namespaceName, String cluster,
                                String releaseTitle, String releaseComment, String branchName,
                                boolean isEmergencyPublish, boolean deleteBranch, ItemChangeSetsDTO changeSets);


}
