package lucky.apollo.portal.entity.bo;

import lucky.apollo.common.entity.dto.NamespaceDTO;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
public class NamespaceInfo {
    private NamespaceDTO baseInfo;
    private Integer itemModifiedCnt;
    private List<ItemInfo> items;
    private String format;
    private Boolean isPublic = false;
    private String parentAppId;
    private String comment;
    /**
     * is the configs hidden to current user?
     */
    private Boolean isConfigHidden = false;

    public void hideItems() {
        setConfigHidden(true);
        items.clear();
        setItemModifiedCnt(0);
    }

    public NamespaceDTO getBaseInfo() {
        return baseInfo;
    }

    public void setBaseInfo(NamespaceDTO baseInfo) {
        this.baseInfo = baseInfo;
    }

    public int getItemModifiedCnt() {
        return itemModifiedCnt;
    }

    public void setItemModifiedCnt(int itemModifiedCnt) {
        this.itemModifiedCnt = itemModifiedCnt;
    }

    public List<ItemInfo> getItems() {
        return items;
    }

    public void setItems(List<ItemInfo> items) {
        this.items = items;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getParentAppId() {
        return parentAppId;
    }

    public void setParentAppId(String parentAppId) {
        this.parentAppId = parentAppId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isConfigHidden() {
        return isConfigHidden;
    }

    public void setConfigHidden(boolean hidden) {
        isConfigHidden = hidden;
    }

    @Override
    public String toString() {
        return "NamespaceInfo{" +
                "baseInfo=" + baseInfo +
                ", itemModifiedCnt=" + itemModifiedCnt +
                ", items=" + items +
                ", format='" + format + '\'' +
                ", isPublic=" + isPublic +
                ", parentAppId='" + parentAppId + '\'' +
                ", comment='" + comment + '\'' +
                ", isConfigHidden=" + isConfigHidden +
                '}';
    }
}