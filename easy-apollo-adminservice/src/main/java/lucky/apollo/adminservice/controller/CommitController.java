package lucky.apollo.adminservice.controller;

import lucky.apollo.common.entity.dto.CommitDTO;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.entity.CommitPO;
import lucky.apollo.core.service.CommitService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@RestController
public class CommitController {

    private final CommitService commitService;

    public CommitController(final CommitService commitService) {
        this.commitService = commitService;
    }

    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/commit")
    public List<CommitDTO> find(@PathVariable String appId, @PathVariable String clusterName,
                                @PathVariable String namespaceName, Pageable pageable) {

        List<CommitPO> commits = commitService.find(appId, clusterName, namespaceName, pageable);
        return BeanUtils.batchTransformWithIgnoreNull(CommitDTO.class, commits);
    }

}