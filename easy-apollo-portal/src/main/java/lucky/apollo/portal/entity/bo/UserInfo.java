package lucky.apollo.portal.entity.bo;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
@Setter
@Getter
public class UserInfo {

    /**
     * userId 等于 name
     */
    private String userId;
    private String name;
    private String email;

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != getClass()){
            return false;
        }
        UserInfo anotherUser = (UserInfo) o;
        return userId.equals(anotherUser.userId);
    }
}