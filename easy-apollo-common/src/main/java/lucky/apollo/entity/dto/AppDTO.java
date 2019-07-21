package lucky.apollo.entity.dto;

import lombok.Getter;
import lombok.Setter;
import lucky.apollo.utils.Validator;

import javax.validation.constraints.Pattern;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
@Setter
@Getter
public class AppDTO extends BaseDTO {

    private Long id;

    private String name;

    @Pattern(
            regexp = Validator.APP_CLUSTER_NAMESPACE_VALIDATOR,
            message = "AppId格式错误: " + Validator.INVALID_APP_CLUSTER_NAMESPACE_MESSAGE
    )
    private String appId;

    private String orgId;

    private String orgName;

    private String ownerName;

    private String ownerEmail;
}