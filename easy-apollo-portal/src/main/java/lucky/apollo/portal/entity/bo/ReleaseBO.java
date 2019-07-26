package lucky.apollo.portal.entity.bo;

import lombok.Data;
import lucky.apollo.common.entity.dto.ReleaseDTO;

import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Data
public class ReleaseBO {

    private ReleaseDTO baseInfo;

    private Set<KeyValueInfo> items;
}