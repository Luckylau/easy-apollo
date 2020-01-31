package lucky.apollo.portal.adminsevice.api;

import com.google.common.base.Joiner;
import lucky.apollo.common.entity.dto.*;
import lucky.apollo.portal.component.rest.RetryableRestTemplate;
import lucky.apollo.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
@Service
public class AdminServiceApi {

    private static final Joiner JOINER = Joiner.on(",");
    @Autowired
    private RetryableRestTemplate retryableRestTemplate;

    @Autowired
    private UserService userService;

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
        return retryableRestTemplate.post("apps", app, AppDTO.class);
    }

    public void updateApp(AppDTO app) {
    }

    public void deleteApp(String appId, String operator) {

    }

    public List<CommitDTO> find(String appId, String cluster, String namespaceName, int page, int size) {
        return Arrays.asList(retryableRestTemplate.get("apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/commit?page={page}&size={size}",
                CommitDTO[].class,
                appId, cluster, namespaceName, page, size));
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

    public List<NamespaceDTO> findNamespace(String appId, String clusterName) {
        NamespaceDTO[] namespaceDTOs = retryableRestTemplate.get("apps/{appId}/clusters/{clusterName}/namespaces",
                NamespaceDTO[].class, appId, clusterName);
        return Arrays.asList(namespaceDTOs);
    }

    public NamespaceDTO loadNamespace(String appId, String namespaceName, String clusterName) {
        return retryableRestTemplate.get("apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}",
                NamespaceDTO.class, appId, clusterName, namespaceName);
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
        return retryableRestTemplate.get("releases/{releaseId}", ReleaseDTO.class, releaseId);
    }

    public List<ReleaseDTO> findReleaseByIds(Set<Long> releaseIds) {
        if (CollectionUtils.isEmpty(releaseIds)) {
            return Collections.emptyList();
        }

        ReleaseDTO[]
                releases =
                retryableRestTemplate.get("/releases?releaseIds={releaseIds}", ReleaseDTO[].class, JOINER.join(releaseIds));
        return Arrays.asList(releases);

    }

    public List<ReleaseDTO> findAllReleases(String appId, String namespaceName, String cluster, int page,
                                            int size) {
        ReleaseDTO[] releaseDTOs = retryableRestTemplate.get("apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/all?page={page}&size={size}",
                ReleaseDTO[].class,
                appId, cluster, namespaceName, page, size);
        return Arrays.asList(releaseDTOs);
    }

    public List<ReleaseDTO> findActiveReleases(String appId, String namespaceName, String cluster,
                                               int page,
                                               int size) {
        ReleaseDTO[] releaseDTOs = retryableRestTemplate.get(
                "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/active?page={page}&size={size}",
                ReleaseDTO[].class,
                appId, cluster, namespaceName, page, size);
        return Arrays.asList(releaseDTOs);
    }

    public ReleaseDTO loadLatestRelease(String appId, String namespace, String cluster) {
        return retryableRestTemplate.get("apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/latest",
                ReleaseDTO.class, appId, cluster, namespace);
    }

    public ReleaseDTO createRelease(String appId, String namespace, String clusterName,
                                    String releaseName, String releaseComment, String operator,
                                    boolean isEmergencyPublish) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8"));
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("name", releaseName);
        parameters.add("comment", releaseComment);
        parameters.add("operator", operator);
        parameters.add("isEmergencyPublish", String.valueOf(isEmergencyPublish));
        HttpEntity<MultiValueMap<String, String>> entity =
                new HttpEntity<>(parameters, headers);
        ReleaseDTO response = retryableRestTemplate.post(
                "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases", entity,
                ReleaseDTO.class, appId, clusterName, namespace);
        return response;
    }

    public ReleaseDTO createGrayDeletionRelease(String appId, String namespace, String cluster,
                                                String releaseName, String releaseComment, String operator,
                                                boolean isEmergencyPublish, Set<String> grayDelKeys) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8"));
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("releaseName", releaseName);
        parameters.add("comment", releaseComment);
        parameters.add("operator", operator);
        parameters.add("isEmergencyPublish", String.valueOf(isEmergencyPublish));
        grayDelKeys.forEach(key -> parameters.add("grayDelKeys", key));
        HttpEntity<MultiValueMap<String, String>> entity =
                new HttpEntity<>(parameters, headers);
        return retryableRestTemplate.post("apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/gray-del-releases", entity,
                ReleaseDTO.class, appId, cluster, namespace);
    }

    public ReleaseDTO updateAndPublish(String appId, String namespace, String cluster,
                                       String releaseName, String releaseComment, String branchName,
                                       boolean isEmergencyPublish, boolean deleteBranch, ItemChangeSetsDTO changeSets) {

        return retryableRestTemplate.post("apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/updateAndPublish?"
                        + "releaseName={releaseName}&releaseComment={releaseComment}&branchName={branchName}"
                        + "&deleteBranch={deleteBranch}&isEmergencyPublish={isEmergencyPublish}",
                changeSets, ReleaseDTO.class, appId, cluster, namespace,
                releaseName, releaseComment, branchName, deleteBranch, isEmergencyPublish);
    }

    public void rollback(long releaseId) {
        retryableRestTemplate.put("releases/{releaseId}/rollback?operator={operator}",
                null, releaseId, userService.getCurrentUser().getUserId());
    }

    public List<ItemDTO> findItems(String appId, String branchName, String namespaceName) {
        ItemDTO[] itemDTOs =
                retryableRestTemplate.get("apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items",
                        ItemDTO[].class, appId, branchName, namespaceName);
        return Arrays.asList(itemDTOs);
    }

    public ItemDTO loadItem(String appId, String namespaceName, String key) {
        return null;
    }

    public void updateItemsByChangeSet(String appId, String namespace, String cluster, ItemChangeSetsDTO changeSets) {
        retryableRestTemplate.post("apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/itemset",
                changeSets, Void.class, appId, cluster, namespace);
    }

    public void updateItem(String appId, String namespace, String cluster, long itemId, ItemDTO item) {
        retryableRestTemplate.put("apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}",
                item, appId, cluster, namespace, itemId);
    }

    public ItemDTO createItem(String appId, String namespace, String cluster, ItemDTO item) {
        return retryableRestTemplate.post("apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items",
                item, ItemDTO.class, appId, cluster, namespace);
    }

    public void deleteItem(long itemId, String operator) {
        retryableRestTemplate.delete("items/{itemId}?operator={operator}", itemId, operator);
    }

    public NamespaceLockDTO getNamespaceLockOwner(String appId, String namespaceName) {
        return null;
    }

    public PageDTO<ReleaseHistoryDTO> findReleaseHistoriesByNamespace(String appId,
                                                                      String namespaceName, String cluster, int page, int size) {
        return retryableRestTemplate.get("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/histories?page={page}&size={size}"
                , type, appId, cluster, namespaceName, page, size).getBody();
    }

    public PageDTO<ReleaseHistoryDTO> findByReleaseIdAndOperation(long releaseId, int operation, int page,
                                                                  int size) {
        return null;
    }

    public PageDTO<ReleaseHistoryDTO> findByPreviousReleaseIdAndOperation(long previousReleaseId,
                                                                          int operation, int page, int size) {
        return null;
    }

    public NamespaceDTO findBranch(String appId,
                                   String namespaceName, String cluster) {
        return retryableRestTemplate.get("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches",
                NamespaceDTO.class, appId, cluster, namespaceName);
    }

    public NamespaceDTO createBranch(String appId,
                                     String namespaceName, String clusterName, String operator) {
        return retryableRestTemplate
                .post("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches?operator={operator}",
                        null, NamespaceDTO.class, appId, clusterName, namespaceName, operator);
    }

    public GrayReleaseRuleDTO findBranchGrayRules(String appId, String cluster, String namespaceName, String branchName) {
        return retryableRestTemplate
                .get("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules",
                        GrayReleaseRuleDTO.class, appId, cluster, namespaceName, branchName);

    }

    public void updateBranchGrayRules(String appId, String namespaceName, String cluster, String branchName, GrayReleaseRuleDTO rules) {
        retryableRestTemplate
                .put("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules",
                        rules, appId, cluster, namespaceName, branchName);

    }

    public void deleteBranch(String appId, String namespaceName, String cluster, String branchName, String operator) {
        retryableRestTemplate.delete(
                "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}?operator={operator}",
                appId, cluster, namespaceName, branchName, operator);
    }

    public List<ClusterDTO> findClustersByApp(String appId) {
        ClusterDTO[] clusterDTOs = retryableRestTemplate.get("apps/{appId}/clusters", ClusterDTO[].class,
                appId);
        return Arrays.asList(clusterDTOs);
    }

    public ClusterDTO loadCluster(String appId, String clusterName) {
        return retryableRestTemplate.get("apps/{appId}/clusters/{clusterName}", ClusterDTO.class,
                appId, clusterName);
    }

    public boolean isClusterUnique(String appId, String clusterName) {
        return retryableRestTemplate
                .get("apps/{appId}/cluster/{clusterName}/unique", Boolean.class,
                        appId, clusterName);

    }

    public ClusterDTO create(ClusterDTO cluster) {
        return retryableRestTemplate.post("apps/{appId}/clusters", cluster, ClusterDTO.class,
                cluster.getAppId());
    }


    public void delete(String appId, String clusterName, String operator) {
        retryableRestTemplate.delete("apps/{appId}/clusters/{clusterName}?operator={operator}", appId, clusterName, operator);
    }

}