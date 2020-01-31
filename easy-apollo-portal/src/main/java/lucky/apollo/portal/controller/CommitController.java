package lucky.apollo.portal.controller;


import lucky.apollo.common.entity.dto.CommitDTO;
import lucky.apollo.portal.adminsevice.api.AdminServiceApi;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@Validated
@RestController
public class CommitController {
    private final AdminServiceApi adminServiceApi;

    public CommitController(final AdminServiceApi adminServiceApi) {
        this.adminServiceApi = adminServiceApi;
    }

    @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/commits")
    public List<CommitDTO> find(@PathVariable String appId, @PathVariable String env,
                                @PathVariable String clusterName, @PathVariable String namespaceName,
                                @Valid @PositiveOrZero(message = "page should be positive or 0") @RequestParam(defaultValue = "0") int page,
                                @Valid @Positive(message = "size should be positive number") @RequestParam(defaultValue = "10") int size) {

        return adminServiceApi.find(appId, clusterName, namespaceName, page, size);
    }
}