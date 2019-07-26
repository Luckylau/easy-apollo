package lucky.apollo.portal.entity.vo;

import lombok.Data;
import lucky.apollo.portal.entity.bo.UserInfo;

import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Data
public class AppRolesAssignedUsers {
    private String appId;
    private Set<UserInfo> masterUsers;
}