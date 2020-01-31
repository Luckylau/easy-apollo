package lucky.apollo.portal.component.txtresolver;

import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ItemDTO;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.common.utils.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author luckylau
 * @Date 2019/10/10
 */
@Component
public class ItemsComparator {

    public ItemChangeSetsDTO compareIgnoreBlankAndCommentItem(long baseNamespaceId, List<ItemDTO> baseItems, List<ItemDTO> targetItems) {
        List<ItemDTO> filteredSourceItems = filterBlankAndCommentItem(baseItems);
        List<ItemDTO> filteredTargetItems = filterBlankAndCommentItem(targetItems);

        Map<String, ItemDTO> sourceItemMap = BeanUtils.mapByKey("key", filteredSourceItems);
        Map<String, ItemDTO> targetItemMap = BeanUtils.mapByKey("key", filteredTargetItems);

        ItemChangeSetsDTO changeSets = new ItemChangeSetsDTO();

        for (ItemDTO item : targetItems) {
            String key = item.getKey();
            ItemDTO sourceItem = sourceItemMap.get(key);
            if (sourceItem == null) {
                //add
                ItemDTO copiedItem = copyItem(item);
                copiedItem.setNamespaceId(baseNamespaceId);
                changeSets.addCreateItem(copiedItem);
                changeSets.addCreateItem(copiedItem);
            } else if (!Objects.equals(sourceItem.getValue(), item.getValue())) {
                //only value & comment can be update
                ItemDTO copiedItem = copyItem(sourceItem);
                copiedItem.setValue(item.getValue());
                copiedItem.setComment(item.getComment());
                changeSets.addUpdateItem(copiedItem);
            }
        }

        for (ItemDTO item : baseItems) {
            String key = item.getKey();
            ItemDTO targetItem = targetItemMap.get(key);
            if (targetItem == null) {
                changeSets.addDeleteItem(item);
            }
        }

        return changeSets;

    }

    private List<ItemDTO> filterBlankAndCommentItem(List<ItemDTO> items) {

        List<ItemDTO> result = new LinkedList<>();

        if (CollectionUtils.isEmpty(items)) {
            return result;
        }

        for (ItemDTO item : items) {
            if (!StringUtils.isEmpty(item.getKey())) {
                result.add(item);
            }
        }

        return result;
    }

    private ItemDTO copyItem(ItemDTO sourceItem) {
        ItemDTO copiedItem = new ItemDTO();
        copiedItem.setKey(sourceItem.getKey());
        copiedItem.setValue(sourceItem.getValue());
        copiedItem.setComment(sourceItem.getComment());
        return copiedItem;

    }

}
