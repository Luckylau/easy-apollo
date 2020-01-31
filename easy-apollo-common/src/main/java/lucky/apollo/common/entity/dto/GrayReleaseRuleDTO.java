package lucky.apollo.common.entity.dto;

import com.google.common.collect.Sets;
import lombok.Data;

import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/10/10
 */
@Data
public class GrayReleaseRuleDTO extends BaseDTO {
    private String appId;

    private String clusterName;

    private String namespaceName;

    private String branchName;

    private Set<GrayReleaseRuleItemDTO> ruleItems;

    private Long releaseId;

    public GrayReleaseRuleDTO(String appId, String clusterName, String namespaceName, String branchName) {
        this.appId = appId;
        this.clusterName = clusterName;
        this.namespaceName = namespaceName;
        this.branchName = branchName;
        this.ruleItems = Sets.newHashSet();
    }
}
