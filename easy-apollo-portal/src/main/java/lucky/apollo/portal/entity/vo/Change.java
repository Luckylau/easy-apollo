package lucky.apollo.portal.entity.vo;

import lombok.Data;
import lucky.apollo.common.entity.EntityPair;
import lucky.apollo.portal.constant.ChangeType;
import lucky.apollo.portal.entity.bo.KeyValueInfo;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Data
public class Change {
    private ChangeType type;
    private EntityPair<KeyValueInfo> entity;

    public Change(ChangeType type, EntityPair<KeyValueInfo> entity) {
        this.type = type;
        this.entity = entity;
    }

}