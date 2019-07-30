package lucky.apollo.core.service;

import lucky.apollo.core.entity.ItemPO;

import java.util.Date;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface ItemService {
    int batchDelete(long namespaceId, String operator);

    ItemPO delete(long id, String operator);

    ItemPO findOne(String appId, String clusterName, String namespaceName, String key);

    ItemPO findLastOne(String appId, String clusterName, String namespaceName);

    ItemPO findLastOne(long namespaceId);

    ItemPO findOne(long itemId);

    List<ItemPO> findItemsWithoutOrdered(Long namespaceId);

    List<ItemPO> findItemsWithoutOrdered(String appId, String clusterName, String namespaceName);

    List<ItemPO> findItemsWithOrdered(Long namespaceId);

    List<ItemPO> findItemsWithOrdered(String appId, String clusterName, String namespaceName);

    List<ItemPO> findItemsModifiedAfterDate(long namespaceId, Date date);

    ItemPO save(ItemPO entity);

    ItemPO update(ItemPO item);
}
