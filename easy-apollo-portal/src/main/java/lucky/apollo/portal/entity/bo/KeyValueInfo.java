package lucky.apollo.portal.entity.bo;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Getter
@Setter
public class KeyValueInfo {

    private String key;
    private String value;

    public KeyValueInfo(String key, String value) {
        this.key = key;
        this.value = value;
    }
}