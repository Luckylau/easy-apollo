package lucky.apollo.portal.entity.vo;

import lombok.Data;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Data
public class Lock {
    private String lockOwner;
    private boolean isEmergencyPublishAllowed;
}