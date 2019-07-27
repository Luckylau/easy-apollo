package lucky.apollo.portal.entity.model;

import lombok.Data;
import lucky.apollo.common.utils.Validator;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@Data
public class AppModel {
    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotBlank(message = "appId cannot be blank")
    @Pattern(
            regexp = Validator.APP_CLUSTER_NAMESPACE_VALIDATOR,
            message = "AppId格式错误: " + Validator.INVALID_APP_CLUSTER_NAMESPACE_MESSAGE
    )
    private String appId;

    @NotBlank(message = "orgId cannot be blank")
    private String orgId;

    @NotBlank(message = "orgName cannot be blank")
    private String orgName;

    @NotBlank(message = "ownerName cannot be blank")
    private String ownerName;

    private Set<String> admins;
}