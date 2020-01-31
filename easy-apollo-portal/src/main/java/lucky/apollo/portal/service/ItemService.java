package lucky.apollo.portal.service;

import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ItemDTO;
import lucky.apollo.portal.entity.model.NamespaceTextModel;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/9/20
 */
public interface ItemService {
    ItemDTO createItem(String appId, String namespaceName, String cluster, ItemDTO item);

    void updateConfigItemByText(NamespaceTextModel model);

    void updateItems(String appId, String namespaceName, String cluster, ItemChangeSetsDTO changeSets);

    void updateItem(String appId, String namespaceName, String cluster, ItemDTO item);

    void deleteItem(long itemId, String userId);

    List<ItemDTO> findItems(String appId, String branchName, String namespaceName);


}
