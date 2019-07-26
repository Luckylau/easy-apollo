package lucky.apollo.portal.api;

import com.google.common.base.Joiner;
import lucky.apollo.common.entity.dto.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
@Service
public class AdminServiceApi {

    private Joiner joiner = Joiner.on(",");

    private ParameterizedTypeReference<PageDTO<InstanceDTO>> pageInstanceDtoType = new ParameterizedTypeReference<PageDTO<InstanceDTO>>() {
    };

    private ParameterizedTypeReference<Map<String, Boolean>> typeReference = new ParameterizedTypeReference<Map<String, Boolean>>() {
    };
    private ParameterizedTypeReference<PageDTO<ReleaseHistoryDTO>> type =
            new ParameterizedTypeReference<PageDTO<ReleaseHistoryDTO>>() {
            };

    public AppDTO loadApp(String appId) {
        return null;
    }

    public AppDTO createApp(AppDTO app) {
        return null;
    }

    public void updateApp(AppDTO app) {
    }

    public void deleteApp(String appId, String operator) {

    }

    public List<CommitDTO> find(String appId, String namespaceName, int page, int size) {
        return null;
    }

    public PageDTO<InstanceDTO> getByRelease(long releaseId, int page, int size) {
        return null;
    }

    public List<InstanceDTO> getByReleasesNotIn(String appId, String clusterName, String namespaceName,
                                                Set<Long> releaseIds) {

        return null;
    }

    public PageDTO<InstanceDTO> getByNamespace(String appId, String clusterName, String namespaceName,
                                               String instanceAppId,
                                               int page, int size) {
        return null;
    }

    public List<NamespaceDTO> findNamespace(String appId) {
        return null;
    }

    public NamespaceDTO loadNamespace(String appId,
                                      String namespaceName) {
        return null;
    }

    public int getInstanceCountByNamespace(String appId, String namespaceName) {
        return 0;
    }

    public NamespaceDTO createNamespace(NamespaceDTO namespace) {
        return null;
    }

    public AppNamespaceDTO createAppNamespace(AppNamespaceDTO appNamespace) {
        return null;
    }

    public AppNamespaceDTO createMissingAppNamespace(AppNamespaceDTO appNamespace) {
        return null;
    }

    public List<AppNamespaceDTO> getAppNamespaces(String appId) {
        return null;
    }

    public void deleteNamespace(String appId, String namespaceName, String operator) {

    }

    public void deleteAppNamespace(String appId, String namespaceName, String operator) {

    }

    public ReleaseDTO loadRelease(long releaseId) {
        return null;
    }

    public List<ReleaseDTO> findReleaseByIds(Set<Long> releaseIds) {
        return null;

    }

    public List<ReleaseDTO> findAllReleases(String appId, String namespaceName, int page,
                                            int size) {
        return null;
    }

    public List<ReleaseDTO> findActiveReleases(String appId, String namespaceName,
                                               int page,
                                               int size) {
        return null;
    }

    public ReleaseDTO loadLatestRelease(String appId, String namespace) {
        return null;
    }

    public ReleaseDTO createRelease(String appId, String namespace,
                                    String releaseName, String releaseComment, String operator,
                                    boolean isEmergencyPublish) {
        return null;
    }

    public ReleaseDTO createGrayDeletionRelease(String appId, String namespace,
                                                String releaseName, String releaseComment, String operator,
                                                boolean isEmergencyPublish, Set<String> grayDelKeys) {
        return null;
    }

    public ReleaseDTO updateAndPublish(String appId, String namespace,
                                       String releaseName, String releaseComment, String branchName,
                                       boolean isEmergencyPublish, boolean deleteBranch, ItemChangeSetsDTO changeSets) {

        return null;

    }

    public void rollback(long releaseId) {

    }

    public List<ItemDTO> findItems(String appId, String namespaceName) {
        return null;
    }

    public ItemDTO loadItem(String appId, String namespaceName, String key) {
        return null;
    }

    public void updateItemsByChangeSet(String appId, String namespace, ItemChangeSetsDTO changeSets) {
    }

    public void updateItem(String appId, String namespace, long itemId, ItemDTO item) {

    }

    public ItemDTO createItem(String appId, String namespace, ItemDTO item) {
        return null;
    }

    public void deleteItem(long itemId, String operator) {

    }

    public NamespaceLockDTO getNamespaceLockOwner(String appId, String namespaceName) {
        return null;
    }

    public PageDTO<ReleaseHistoryDTO> findReleaseHistoriesByNamespace(String appId,
                                                                      String namespaceName, int page, int size) {
        return null;
    }

    public PageDTO<ReleaseHistoryDTO> findByReleaseIdAndOperation(long releaseId, int operation, int page,
                                                                  int size) {
        return null;
    }

    public PageDTO<ReleaseHistoryDTO> findByPreviousReleaseIdAndOperation(long previousReleaseId,
                                                                          int operation, int page, int size) {
        return null;
    }

}