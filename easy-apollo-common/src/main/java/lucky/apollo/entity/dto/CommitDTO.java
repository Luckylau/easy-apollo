package lucky.apollo.entity.dto;

import lombok.Data;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@Data
public class CommitDTO extends BaseDTO {
    private String changeSets;

    private String appId;

    private String clusterName;

    private String namespaceName;

    private String comment;
}