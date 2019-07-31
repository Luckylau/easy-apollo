package lucky.apollo.adminservice.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lucky.apollo.common.entity.dto.PageDTO;
import lucky.apollo.common.entity.dto.ReleaseHistoryDTO;
import lucky.apollo.core.entity.ReleaseHistoryPO;
import lucky.apollo.core.service.ReleaseHistoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@RestController
public class ReleaseHistoryController {

    private final ReleaseHistoryService releaseHistoryService;
    private Gson gson = new Gson();
    private Type configurationTypeReference = new TypeToken<Map<String, Object>>() {
    }.getType();

    public ReleaseHistoryController(final ReleaseHistoryService releaseHistoryService) {
        this.releaseHistoryService = releaseHistoryService;
    }

    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/histories")
    public PageDTO<ReleaseHistoryDTO> findReleaseHistoriesByNamespace(
            @PathVariable String appId, @PathVariable String clusterName,
            @PathVariable String namespaceName,
            Pageable pageable) {

        Page<ReleaseHistoryPO> result = releaseHistoryService.findReleaseHistoriesByNamespace(appId, clusterName,
                namespaceName, pageable);
        return transform2PageDTO(result, pageable);
    }


    @GetMapping("/releases/histories/by_release_id_and_operation")
    public PageDTO<ReleaseHistoryDTO> findReleaseHistoryByReleaseIdAndOperation(
            @RequestParam("releaseId") long releaseId,
            @RequestParam("operation") int operation,
            Pageable pageable) {

        Page<ReleaseHistoryPO> result = releaseHistoryService.findByReleaseIdAndOperation(releaseId, operation, pageable);

        return transform2PageDTO(result, pageable);
    }

    @GetMapping("/releases/histories/by_previous_release_id_and_operation")
    public PageDTO<ReleaseHistoryDTO> findReleaseHistoryByPreviousReleaseIdAndOperation(
            @RequestParam("previousReleaseId") long previousReleaseId,
            @RequestParam("operation") int operation,
            Pageable pageable) {

        Page<ReleaseHistoryPO> result = releaseHistoryService.findByPreviousReleaseIdAndOperation(previousReleaseId, operation, pageable);

        return transform2PageDTO(result, pageable);

    }

    private PageDTO<ReleaseHistoryDTO> transform2PageDTO(Page<ReleaseHistoryPO> releaseHistoriesPage, Pageable pageable) {
        if (!releaseHistoriesPage.hasContent()) {
            return null;
        }

        List<ReleaseHistoryPO> releaseHistories = releaseHistoriesPage.getContent();
        List<ReleaseHistoryDTO> releaseHistoryDTOs = new ArrayList<>(releaseHistories.size());
        for (ReleaseHistoryPO releaseHistory : releaseHistories) {
            releaseHistoryDTOs.add(transformReleaseHistory2DTO(releaseHistory));
        }

        return new PageDTO<>(releaseHistoryDTOs, pageable, releaseHistoriesPage.getTotalElements());
    }

    private ReleaseHistoryDTO transformReleaseHistory2DTO(ReleaseHistoryPO releaseHistory) {
        ReleaseHistoryDTO dto = new ReleaseHistoryDTO();
        BeanUtils.copyProperties(releaseHistory, dto, "operationContext");
        dto.setOperationContext(gson.fromJson(releaseHistory.getOperationContext(),
                configurationTypeReference));

        return dto;
    }
}