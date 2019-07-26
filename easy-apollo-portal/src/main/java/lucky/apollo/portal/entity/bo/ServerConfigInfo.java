package lucky.apollo.portal.entity.bo;

import lombok.Data;

/**
 * @Author luckylau
 * @Date 2019/7/15
 */
@Data
public class ServerConfigInfo {

    private String key;

    private String value;

    private String comment;

    private boolean isDeleted = false;

}