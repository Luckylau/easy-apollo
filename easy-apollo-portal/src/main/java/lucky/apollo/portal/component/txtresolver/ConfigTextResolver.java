package lucky.apollo.portal.component.txtresolver;

import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ItemDTO;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/9/20
 */
public interface ConfigTextResolver {

    ItemChangeSetsDTO resolve(long namespaceId, String configText, List<ItemDTO> baseItems);
}
