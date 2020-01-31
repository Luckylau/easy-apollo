package lucky.apollo.portal.service.impl;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import lucky.apollo.common.constant.GsonType;
import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ReleaseDTO;
import lucky.apollo.common.utils.StringUtils;
import lucky.apollo.portal.adminsevice.api.AdminServiceApi;
import lucky.apollo.portal.constant.ChangeType;
import lucky.apollo.portal.entity.bo.KeyValueInfo;
import lucky.apollo.portal.entity.bo.ReleaseBO;
import lucky.apollo.portal.entity.model.NamespaceGrayDelReleaseModel;
import lucky.apollo.portal.entity.model.NamespaceReleaseModel;
import lucky.apollo.portal.entity.vo.ReleaseCompareResult;
import lucky.apollo.portal.service.ReleaseService;
import lucky.apollo.portal.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @Author luckylau
 * @Date 2019/9/23
 */
@Service
public class ReleaseServiceImpl implements ReleaseService {

    private static final Gson gson = new Gson();

    private final UserService userService;

    private final AdminServiceApi adminServiceApi;

    public ReleaseServiceImpl(UserService userService, AdminServiceApi adminServiceApi) {
        this.userService = userService;
        this.adminServiceApi = adminServiceApi;
    }

    @Override
    public ReleaseDTO publish(NamespaceReleaseModel model) {
        boolean isEmergencyPublish = model.getIsEmergencyPublish();
        String appId = model.getAppId();
        String namespaceName = model.getNamespaceName();
        String releaseBy = StringUtils.isEmpty(model.getReleasedBy()) ?
                userService.getCurrentUser().getUserId() : model.getReleasedBy();

        return adminServiceApi.createRelease(appId, namespaceName, model.getClusterName(),
                model.getReleaseTitle(), model.getReleaseComment(),
                releaseBy, isEmergencyPublish);

    }

    @Override
    public ReleaseDTO publish(NamespaceGrayDelReleaseModel model, String releaseBy) {
        boolean isEmergencyPublish = model.getIsEmergencyPublish();
        String appId = model.getAppId();
        String namespaceName = model.getNamespaceName();

        return adminServiceApi.createGrayDeletionRelease(appId, namespaceName, model.getClusterName(), model.getReleaseTitle(), model.getReleaseComment(),
                releaseBy, isEmergencyPublish, model.getGrayDelKeys());
    }

    @Override
    public List<ReleaseBO> findAllReleases(String appId, String namespaceName, String cluster, int page, int size) {
        List<ReleaseDTO> releaseDTOs = adminServiceApi.findAllReleases(appId, namespaceName, cluster, page, size);

        if (CollectionUtils.isEmpty(releaseDTOs)) {
            return Collections.emptyList();
        }

        List<ReleaseBO> releases = new LinkedList<>();
        for (ReleaseDTO releaseDTO : releaseDTOs) {
            ReleaseBO release = new ReleaseBO();
            release.setBaseInfo(releaseDTO);

            Set<KeyValueInfo> kvEntities = new LinkedHashSet<>();
            Map<String, String> configurations = gson.fromJson(releaseDTO.getConfigurations(), GsonType.CONFIG);
            Set<Map.Entry<String, String>> entries = configurations.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                kvEntities.add(new KeyValueInfo(entry.getKey(), entry.getValue()));
            }
            release.setItems(kvEntities);
            //为了减少数据量
            releaseDTO.setConfigurations("");
            releases.add(release);
        }

        return releases;
    }

    @Override
    public List<ReleaseDTO> findActiveReleases(String appId, String namespaceName, String cluster, int page, int size) {
        return adminServiceApi.findActiveReleases(appId, namespaceName, cluster, page, size);
    }

    @Override
    public List<ReleaseDTO> findReleaseByIds(Set<Long> releaseIds) {
        return adminServiceApi.findReleaseByIds(releaseIds);
    }

    @Override
    public ReleaseDTO loadLatestRelease(String appId, String namespaceName, String cluster) {
        return adminServiceApi.loadLatestRelease(appId, namespaceName, cluster);
    }

    @Override
    public void rollback(long releaseId) {
        adminServiceApi.rollback(releaseId);
    }

    @Override
    public ReleaseCompareResult compare(long baseReleaseId, long toCompareReleaseId) {
        ReleaseDTO baseRelease = null;
        ReleaseDTO toCompareRelease = null;
        if (baseReleaseId != 0) {
            baseRelease = adminServiceApi.loadRelease(baseReleaseId);
        }

        if (toCompareReleaseId != 0) {
            toCompareRelease = adminServiceApi.loadRelease(toCompareReleaseId);
        }

        Map<String, String> baseReleaseConfiguration = baseRelease == null ? new HashMap<>() :
                gson.fromJson(baseRelease.getConfigurations(), GsonType.CONFIG);
        Map<String, String> toCompareReleaseConfiguration = toCompareRelease == null ? new HashMap<>() :
                gson.fromJson(toCompareRelease.getConfigurations(),
                        GsonType.CONFIG);

        ReleaseCompareResult compareResult = new ReleaseCompareResult();

        //added and modified in firstRelease
        for (Map.Entry<String, String> entry : baseReleaseConfiguration.entrySet()) {
            String key = entry.getKey();
            String firstValue = entry.getValue();
            String secondValue = toCompareReleaseConfiguration.get(key);
            //added
            if (secondValue == null) {
                compareResult.addEntityPair(ChangeType.DELETED, new KeyValueInfo(key, firstValue),
                        new KeyValueInfo(key, null));
            } else if (!Objects.equal(firstValue, secondValue)) {
                compareResult.addEntityPair(ChangeType.MODIFIED, new KeyValueInfo(key, firstValue),
                        new KeyValueInfo(key, secondValue));
            }

        }

        //deleted in firstRelease
        for (Map.Entry<String, String> entry : toCompareReleaseConfiguration.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (baseReleaseConfiguration.get(key) == null) {
                compareResult
                        .addEntityPair(ChangeType.ADDED, new KeyValueInfo(key, ""), new KeyValueInfo(key, value));
            }

        }

        return compareResult;
    }

    @Override
    public ReleaseCompareResult compare(ReleaseDTO baseRelease, ReleaseDTO toCompareRelease) {
        return null;
    }

    @Override
    public ReleaseDTO updateAndPublish(String appId, String namespaceName, String cluster, String releaseTitle, String releaseComment, String branchName, boolean isEmergencyPublish, boolean deleteBranch, ItemChangeSetsDTO changeSets) {
        return adminServiceApi.updateAndPublish(appId, namespaceName, cluster, releaseTitle, releaseComment, branchName, isEmergencyPublish, deleteBranch, changeSets);
    }
}
