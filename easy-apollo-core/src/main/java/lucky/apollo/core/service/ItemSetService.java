package lucky.apollo.core.service;

import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.core.entity.NamespacePO;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface ItemSetService {

    ItemChangeSetsDTO updateSet(NamespacePO namespace, ItemChangeSetsDTO changeSets);

    ItemChangeSetsDTO updateSet(String appId, String clusterName,
                                String namespaceName, ItemChangeSetsDTO changeSet);

}
