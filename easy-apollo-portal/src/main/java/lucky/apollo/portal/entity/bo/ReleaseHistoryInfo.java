package lucky.apollo.portal.entity.bo;

import lombok.Data;
import lucky.apollo.common.entity.EntityPair;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Data
public class ReleaseHistoryInfo {
    private long id;

    private String appId;

    private String clusterName;

    private String namespaceName;

    private String branchName;

    private String operator;

    private long releaseId;

    private String releaseTitle;

    private String releaseComment;

    private Date releaseTime;

    private String releaseTimeFormatted;

    private List<EntityPair<String>> configuration;

    private long previousReleaseId;

    private int operation;

    private Map<String, Object> operationContext;
}