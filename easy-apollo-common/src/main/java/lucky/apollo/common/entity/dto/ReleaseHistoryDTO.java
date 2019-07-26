package lucky.apollo.common.entity.dto;

import lombok.Data;

import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Data
public class ReleaseHistoryDTO extends BaseDTO {
    private Long id;

    private String appId;

    private String clusterName;

    private String namespaceName;

    private String branchName;

    private long releaseId;

    private long previousReleaseId;

    private int operation;

    private Map<String, Object> operationContext;
}