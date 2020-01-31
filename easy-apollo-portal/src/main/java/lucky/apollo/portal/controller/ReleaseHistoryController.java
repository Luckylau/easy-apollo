package lucky.apollo.portal.controller;

import com.google.gson.Gson;
import lucky.apollo.common.constant.GsonType;
import lucky.apollo.common.entity.EntityPair;
import lucky.apollo.common.entity.dto.PageDTO;
import lucky.apollo.common.entity.dto.ReleaseDTO;
import lucky.apollo.common.entity.dto.ReleaseHistoryDTO;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.portal.adminsevice.api.AdminServiceApi;
import lucky.apollo.portal.entity.bo.ReleaseHistoryInfo;
import lucky.apollo.portal.resolver.PermissionValidator;
import lucky.apollo.portal.utils.RelativeDateFormatUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@RestController
public class ReleaseHistoryController {

    private static final Gson gson = new Gson();

    private final PermissionValidator permissionValidator;

    private final AdminServiceApi adminServiceApi;

    public ReleaseHistoryController(final AdminServiceApi adminServiceApi, final PermissionValidator permissionValidator) {
        this.adminServiceApi = adminServiceApi;
        this.permissionValidator = permissionValidator;
    }

    @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases/histories")
    public List<ReleaseHistoryInfo> findReleaseHistoriesByNamespace(@PathVariable String appId,
                                                                    @PathVariable String env,
                                                                    @PathVariable String clusterName,
                                                                    @PathVariable String namespaceName,
                                                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                                                    @RequestParam(value = "size", defaultValue = "10") int size) {

        PageDTO<ReleaseHistoryDTO> result = adminServiceApi.findReleaseHistoriesByNamespace(appId, namespaceName, clusterName, page, size);
        if (result == null || !result.hasContent()) {
            return Collections.emptyList();
        }

        List<ReleaseHistoryDTO> content = result.getContent();
        Set<Long> releaseIds = new HashSet<>();
        for (ReleaseHistoryDTO releaseHistoryDTO : content) {
            long releaseId = releaseHistoryDTO.getReleaseId();
            if (releaseId != 0) {
                releaseIds.add(releaseId);
            }
        }

        List<ReleaseDTO> releases = adminServiceApi.findReleaseByIds(releaseIds);

        return transformReleaseHistoryDTO2BO(content, releases);
    }

    private List<ReleaseHistoryInfo> transformReleaseHistoryDTO2BO(List<ReleaseHistoryDTO> source,
                                                                   List<ReleaseDTO> releases) {

        Map<Long, ReleaseDTO> releasesMap = BeanUtils.mapByKey("id", releases);

        List<ReleaseHistoryInfo> bos = new ArrayList<>(source.size());
        for (ReleaseHistoryDTO dto : source) {
            ReleaseDTO release = releasesMap.get(dto.getReleaseId());
            bos.add(transformReleaseHistoryDTO2BO(dto, release));
        }

        return bos;
    }

    private ReleaseHistoryInfo transformReleaseHistoryDTO2BO(ReleaseHistoryDTO dto, ReleaseDTO release) {
        ReleaseHistoryInfo bo = new ReleaseHistoryInfo();
        bo.setId(dto.getId());
        bo.setAppId(dto.getAppId());
        bo.setClusterName(dto.getClusterName());
        bo.setNamespaceName(dto.getNamespaceName());
        bo.setBranchName(dto.getBranchName());
        bo.setReleaseId(dto.getReleaseId());
        bo.setPreviousReleaseId(dto.getPreviousReleaseId());
        bo.setOperator(dto.getDataChangeCreatedBy());
        bo.setOperation(dto.getOperation());
        Date releaseTime = dto.getDataChangeLastModifiedTime();
        bo.setReleaseTime(releaseTime);
        bo.setReleaseTimeFormatted(RelativeDateFormatUtils.format(releaseTime));
        bo.setOperationContext(dto.getOperationContext());
        //set release info
        setReleaseInfoToReleaseHistoryBO(bo, release);

        return bo;
    }

    private void setReleaseInfoToReleaseHistoryBO(ReleaseHistoryInfo bo, ReleaseDTO release) {
        if (release != null) {
            bo.setReleaseTitle(release.getName());
            bo.setReleaseComment(release.getComment());

            Map<String, String> configuration = gson.fromJson(release.getConfigurations(), GsonType.CONFIG);
            List<EntityPair<String>> items = new ArrayList<>(configuration.size());
            for (Map.Entry<String, String> entry : configuration.entrySet()) {
                EntityPair<String> entityPair = new EntityPair<>(entry.getKey(), entry.getValue());
                items.add(entityPair);
            }
            bo.setConfiguration(items);

        } else {
            bo.setReleaseTitle("no release information");
            bo.setConfiguration(null);
        }
    }

}