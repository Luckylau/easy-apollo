package lucky.apollo.common.entity.dto;

import lombok.Data;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
@Data
public class ReleaseDTO extends BaseDTO {
    private Long id;

    private String releaseKey;

    private String name;

    private String appId;

    private String namespaceName;

    private String configurations;

    private String comment;

    private Boolean isAbandoned;
}