package lucky.apollo.portal.entity.model;

import lombok.Data;
import lucky.apollo.common.utils.StringUtils;


/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Data
public class NamespaceReleaseModel implements Verifiable {

    private String appId;
    private String namespaceName;
    private String clusterName;
    private String releaseTitle;
    private String releaseComment;
    private String releasedBy;
    private Boolean isEmergencyPublish;

    @Override
    public boolean isInvalid() {
        return StringUtils.isContainEmpty(appId, clusterName, namespaceName, releaseTitle);
    }
}