package lucky.apollo.portal.entity.vo;

import lombok.Data;
import lucky.apollo.common.constant.Env;
import lucky.apollo.common.entity.dto.ClusterDTO;

/**
 * @Author luckylau
 * @Date 2019/8/12
 */
@Data
public class EnvClusterInfo {
    private Env env;

    private ClusterDTO clusterDTO;

}
