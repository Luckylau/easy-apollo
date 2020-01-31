package lucky.apollo.portal.component.txtresolver;

import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ItemDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/9/20
 */
@Component("fileTextResolver")
public class FileTextResolver implements ConfigTextResolver {
    @Override
    public ItemChangeSetsDTO resolve(long namespaceId, String configText, List<ItemDTO> baseItems) {
        ItemChangeSetsDTO changeSets = new ItemChangeSetsDTO();
        if (CollectionUtils.isEmpty(baseItems) && StringUtils.isEmpty(configText)) {
            return changeSets;
        }
        if (CollectionUtils.isEmpty(baseItems)) {
            changeSets.addCreateItem(createItem(namespaceId, 0, configText));
        } else {
            ItemDTO beforeItem = baseItems.get(0);
            //update
            if (!configText.equals(beforeItem.getValue())) {
                changeSets.addUpdateItem(createItem(namespaceId, beforeItem.getId(), configText));
            }
        }

        return changeSets;
    }

    private ItemDTO createItem(long namespaceId, long itemId, String value) {
        ItemDTO item = new ItemDTO();
        item.setId(itemId);
        item.setNamespaceId(namespaceId);
        item.setValue(value);
        item.setLineNum(1);
        item.setKey(ConfigConsts.CONFIG_FILE_CONTENT_KEY);
        return item;
    }
}
