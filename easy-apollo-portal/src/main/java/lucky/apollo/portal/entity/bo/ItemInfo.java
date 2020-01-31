package lucky.apollo.portal.entity.bo;

import lucky.apollo.common.entity.dto.ItemDTO;


/**
 * @Author luckylau
 * @Date 2019/7/19
 */
public class ItemInfo {
    private ItemDTO item;
    private Boolean isModified = false;
    private Boolean isDeleted = false;
    private String oldValue;
    private String newValue;

    public ItemDTO getItem() {
        return item;
    }

    public void setItem(ItemDTO item) {
        this.item = item;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean isModified) {
        this.isModified = isModified;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return "ItemInfo{" +
                "item=" + item +
                ", isModified=" + isModified +
                ", isDeleted=" + isDeleted +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }
}