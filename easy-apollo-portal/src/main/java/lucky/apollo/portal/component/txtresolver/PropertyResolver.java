package lucky.apollo.portal.component.txtresolver;

import com.google.common.base.Strings;
import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ItemDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author luckylau
 * @Date 2019/9/20
 */
@Component("propertyResolver")
public class PropertyResolver implements ConfigTextResolver {

    private static final String KV_SEPARATOR = "=";

    private static final String ITEM_SEPARATOR = "\n";

    @Override
    public ItemChangeSetsDTO resolve(long namespaceId, String configText, List<ItemDTO> baseItems) {
        Map<Integer, ItemDTO> oldLineNumMapItem = BeanUtils.mapByKey("lineNum", baseItems);
        Map<String, ItemDTO> oldKeyMapItem = BeanUtils.mapByKey("key", baseItems);

        //remove comment and blank item map.
        oldKeyMapItem.remove("");

        String[] newItems = configText.split(ITEM_SEPARATOR);
        if (isHasRepeatKey(newItems)) {
            throw new BadRequestException("config text has repeat key please check.");
        }

        ItemChangeSetsDTO changeSets = new ItemChangeSetsDTO();
        //use for delete blank and comment item
        Map<Integer, String> newLineNumMapItem = new HashMap<>(16);
        int lineCounter = 1;
        for (String newItem : newItems) {
            newItem = newItem.trim();
            newLineNumMapItem.put(lineCounter, newItem);
            ItemDTO oldItemByLine = oldLineNumMapItem.get(lineCounter);
            if (isCommentItem(newItem)) {
                handleCommentLine(namespaceId, oldItemByLine, newItem, lineCounter, changeSets);
            } else if (isBlankItem(newItem)) {
                handleBlankLine(namespaceId, oldItemByLine, lineCounter, changeSets);
            } else {
                handleNormalLine(namespaceId, oldKeyMapItem, newItem, lineCounter, changeSets);
            }
        }

        deleteCommentAndBlankItem(oldLineNumMapItem, newLineNumMapItem, changeSets);
        deleteNormalKVItem(oldKeyMapItem, changeSets);
        return changeSets;
    }


    private void deleteCommentAndBlankItem(Map<Integer, ItemDTO> oldLineNumMapItem,
                                           Map<Integer, String> newLineNumMapItem,
                                           ItemChangeSetsDTO changeSets) {

        for (Map.Entry<Integer, ItemDTO> entry : oldLineNumMapItem.entrySet()) {
            int lineNum = entry.getKey();
            ItemDTO oldItem = entry.getValue();
            String newItem = newLineNumMapItem.get(lineNum);

            //1. old is blank by now is not
            //2.old is comment by now is not exist or modified
            boolean oldIsBlankAndNowIsNot = isBlankItem(oldItem) && !isBlankItem(newItem);
            boolean oldIsCommentNowIsNotExistOrModified = isCommentItem(oldItem) && (newItem == null || !newItem.equals(oldItem.getComment()));
            if (oldIsBlankAndNowIsNot || oldIsCommentNowIsNotExistOrModified) {
                changeSets.addDeleteItem(oldItem);
            }
        }
    }

    private void deleteNormalKVItem(Map<String, ItemDTO> baseKeyMapItem, ItemChangeSetsDTO changeSets) {
        //surplus item is to be deleted
        for (Map.Entry<String, ItemDTO> entry : baseKeyMapItem.entrySet()) {
            changeSets.addDeleteItem(entry.getValue());
        }
    }


    private void handleNormalLine(Long namespaceId, Map<String, ItemDTO> keyMapOldItem, String newItem,
                                  int lineCounter, ItemChangeSetsDTO changeSets) {

        String[] kv = parseKeyValueFromItem(newItem);

        if (kv == null) {
            throw new BadRequestException("line:" + lineCounter + " key value must separate by '='");
        }

        String newKey = kv[0];
        //handle user input \n
        String newValue = kv[1].replace("\\n", "\n");

        ItemDTO oldItem = keyMapOldItem.get(newKey);

        if (oldItem == null) {
            //new item
            changeSets.addCreateItem(buildNormalItem(0L, namespaceId, newKey, newValue, "", lineCounter));
        } else if (!newValue.equals(oldItem.getValue()) || lineCounter != oldItem.getLineNum()) {
            //update item
            changeSets.addUpdateItem(
                    buildNormalItem(oldItem.getId(), namespaceId, newKey, newValue, oldItem.getComment(),
                            lineCounter));
        }
        keyMapOldItem.remove(newKey);
    }

    private void handleCommentLine(Long namespaceId, ItemDTO oldItemByLine, String newItem, int lineCounter, ItemChangeSetsDTO changeSets) {
        String oldComment = oldItemByLine == null ? "" : oldItemByLine.getComment();
        //create comment. implement update comment by delete old comment and create new comment
        if (!(isCommentItem(oldItemByLine) && newItem.equals(oldComment))) {
            changeSets.addCreateItem(buildCommentItem(0L, namespaceId, newItem, lineCounter));
        }
    }

    private void handleBlankLine(Long namespaceId, ItemDTO oldItem, int lineCounter, ItemChangeSetsDTO changeSets) {
        if (!isBlankItem(oldItem)) {
            changeSets.addCreateItem(buildBlankItem(0L, namespaceId, lineCounter));
        }
    }

    private boolean isBlankItem(ItemDTO item) {
        return item != null && "".equals(item.getKey()) && "".equals(item.getComment());
    }

    private ItemDTO buildBlankItem(Long id, Long namespaceId, int lineNum) {
        return buildNormalItem(id, namespaceId, "", "", "", lineNum);
    }


    private boolean isCommentItem(ItemDTO item) {
        return item != null && "".equals(item.getKey())
                && (item.getComment().startsWith("#") || item.getComment().startsWith("!"));
    }


    private ItemDTO buildCommentItem(Long id, Long namespaceId, String comment, int lineNum) {
        return buildNormalItem(id, namespaceId, "", "", comment, lineNum);
    }

    private ItemDTO buildNormalItem(Long id, Long namespaceId, String key, String value, String comment, int lineNum) {
        ItemDTO item = new ItemDTO(key, value, comment, lineNum);
        item.setId(id);
        item.setNamespaceId(namespaceId);
        return item;
    }


    private boolean isHasRepeatKey(String[] newItems) {
        Set<String> keys = new HashSet<>();
        int lineCounter = 1;
        int keyCount = 0;
        for (String item : newItems) {
            if (!isCommentItem(item) && !isBlankItem(item)) {
                keyCount++;
                String[] kv = parseKeyValueFromItem(item);
                if (kv != null) {
                    keys.add(kv[0].toLowerCase());
                } else {
                    throw new BadRequestException("line:" + lineCounter + " key value must separate by '='");
                }
            }
            lineCounter++;
        }
        return keyCount > keys.size();
    }

    private String[] parseKeyValueFromItem(String item) {
        int kvSeparator = item.lastIndexOf(KV_SEPARATOR);
        if (kvSeparator == -1) {
            return null;
        }
        String[] kv = new String[2];
        kv[0] = item.substring(0, kvSeparator).trim();
        kv[1] = item.substring(kvSeparator + 1, item.length()).trim();
        return kv;
    }

    private boolean isCommentItem(String line) {
        return line != null && (line.startsWith("#") || line.startsWith("!"));
    }

    private boolean isBlankItem(String line) {
        return Strings.nullToEmpty(line).trim().isEmpty();
    }

}
