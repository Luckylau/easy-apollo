package lucky.apollo.common.entity.dto;

import com.google.common.collect.Sets;

import java.util.Set;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * @Author luckylau
 * @Date 2019/10/10
 */
public class GrayReleaseRuleItemDTO {
    public static final String ALL_IP = "*";

    private String clientAppId;
    private Set<String> clientIpList;

    public GrayReleaseRuleItemDTO(String clientAppId) {
        this(clientAppId, Sets.newHashSet());
    }

    public GrayReleaseRuleItemDTO(String clientAppId, Set<String> clientIpList) {
        this.clientAppId = clientAppId;
        this.clientIpList = clientIpList;
    }

    public String getClientAppId() {
        return clientAppId;
    }

    public Set<String> getClientIpList() {
        return clientIpList;
    }

    public boolean matches(String clientAppId, String clientIp) {
        return appIdMatches(clientAppId) && ipMatches(clientIp);
    }

    private boolean appIdMatches(String clientAppId) {
        return this.clientAppId.equals(clientAppId);
    }

    private boolean ipMatches(String clientIp) {
        return this.clientIpList.contains(ALL_IP) || clientIpList.contains(clientIp);
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("clientAppId", clientAppId)
                .add("clientIpList", clientIpList).toString();
    }
}
