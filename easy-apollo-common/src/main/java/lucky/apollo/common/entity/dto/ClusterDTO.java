package lucky.apollo.common.entity.dto;

import lombok.Data;
import lucky.apollo.common.utils.Validator;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 *
 *
 * @Author luckylau
 * @Date 2019/8/12
 */
@Data
public class ClusterDTO extends BaseDTO {

    @NotBlank(message = "cluster name cannot be blank")
    @Pattern(
            regexp = Validator.APP_CLUSTER_NAMESPACE_VALIDATOR,
            message = "Cluster格式错误: " + Validator.INVALID_APP_CLUSTER_NAMESPACE_MESSAGE
    )
    private String name;

    private String appId;

    private Long parentClusterId;
}
