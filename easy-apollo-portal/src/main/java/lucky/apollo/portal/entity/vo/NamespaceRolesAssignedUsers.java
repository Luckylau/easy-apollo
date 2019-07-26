package lucky.apollo.portal.entity.vo;

import lombok.Data;
import lucky.apollo.portal.entity.bo.UserInfo;

import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Data
public class NamespaceRolesAssignedUsers {

    private String appId;
    private String namespaceName;

    private Set<UserInfo> modifyRoleUsers;
    private Set<UserInfo> releaseRoleUsers;
}