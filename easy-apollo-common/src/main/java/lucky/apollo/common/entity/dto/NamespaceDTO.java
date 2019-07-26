package lucky.apollo.common.entity.dto;

import lombok.Data;
import lucky.apollo.common.utils.Validator;

import javax.validation.constraints.Pattern;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
@Data
public class NamespaceDTO extends BaseDTO {
    private long id;

    private String appId;

    private String clusterName;

    @Pattern(
            regexp = Validator.APP_CLUSTER_NAMESPACE_VALIDATOR,
            message = "Namespace格式错误: " + Validator.INVALID_APP_CLUSTER_NAMESPACE_MESSAGE
    )
    private String namespaceName;
}