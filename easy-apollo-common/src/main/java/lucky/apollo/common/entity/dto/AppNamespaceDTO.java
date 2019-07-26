package lucky.apollo.common.entity.dto;

import lombok.Data;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
@Data
public class AppNamespaceDTO extends lucky.apollo.common.entity.dto.BaseDTO {
    private long id;

    private String name;

    private String appId;

    private String comment;

    private String format;

    private Boolean isPublic = false;
}