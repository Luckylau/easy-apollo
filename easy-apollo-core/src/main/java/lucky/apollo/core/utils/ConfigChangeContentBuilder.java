package lucky.apollo.core.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lucky.apollo.common.utils.StringUtils;
import lucky.apollo.core.entity.ItemPO;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
public class ConfigChangeContentBuilder {

    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private List<ItemPO> createItems = new LinkedList<>();
    private List<ItemPair> updateItems = new LinkedList<>();
    private List<ItemPO> deleteItems = new LinkedList<>();


    public ConfigChangeContentBuilder createItem(ItemPO item) {
        if (!StringUtils.isEmpty(item.getKey())) {
            createItems.add(cloneItem(item));
        }
        return this;
    }

    public ConfigChangeContentBuilder updateItem(ItemPO oldItem, ItemPO newItem) {
        if (!oldItem.getValue().equals(newItem.getValue())) {
            ItemPair itemPair = new ItemPair(cloneItem(oldItem), cloneItem(newItem));
            updateItems.add(itemPair);
        }
        return this;
    }

    public ConfigChangeContentBuilder deleteItem(ItemPO item) {
        if (!StringUtils.isEmpty(item.getKey())) {
            deleteItems.add(cloneItem(item));
        }
        return this;
    }

    public boolean hasContent() {
        return !createItems.isEmpty() || !updateItems.isEmpty() || !deleteItems.isEmpty();
    }

    public String build() {
        //因为事务第一段提交并没有更新时间,所以build时统一更新
        Date now = new Date();

        for (ItemPO item : createItems) {
            item.setDataChangeLastModifiedTime(now);
        }

        for (ItemPair item : updateItems) {
            item.newItem.setDataChangeLastModifiedTime(now);
        }

        for (ItemPO item : deleteItems) {
            item.setDataChangeLastModifiedTime(now);
        }
        return gson.toJson(this);
    }

    ItemPO cloneItem(ItemPO source) {
        ItemPO target = new ItemPO();

        BeanUtils.copyProperties(source, target);

        return target;
    }

    static class ItemPair {

        ItemPO oldItem;
        ItemPO newItem;

        public ItemPair(ItemPO oldItem, ItemPO newItem) {
            this.oldItem = oldItem;
            this.newItem = newItem;
        }
    }

}
