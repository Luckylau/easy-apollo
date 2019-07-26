package lucky.apollo.portal.entity.bo;

import lombok.Data;
import lucky.apollo.common.entity.dto.NamespaceDTO;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
@Data
public class NamespaceInfo {
    private NamespaceDTO baseInfo;
    private Integer itemModifiedCnt;
    private List<ItemInfo> items;
    private String format;
    private Boolean isPublic;
    private String parentAppId;
    private String comment;
    /**
     * is the configs hidden to current user?
     */
    private Boolean isConfigHidden;

    public void hideItems() {
        setIsConfigHidden(true);
        items.clear();
        setItemModifiedCnt(0);
    }
}