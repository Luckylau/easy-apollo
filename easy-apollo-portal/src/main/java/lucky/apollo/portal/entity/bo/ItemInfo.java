package lucky.apollo.portal.entity.bo;

import lombok.Data;
import lucky.apollo.common.entity.dto.ItemDTO;


/**
 * @Author luckylau
 * @Date 2019/7/19
 */
@Data
public class ItemInfo {
    private ItemDTO item;
    private boolean isModified;
    private boolean isDeleted;
    private String oldValue;
    private String newValue;
}