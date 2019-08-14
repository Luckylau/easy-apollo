package lucky.apollo.common.entity.dto;

import lombok.Data;

/**
 * 只有一个默认集群 default
 *
 * @Author luckylau
 * @Date 2019/8/12
 */
@Data
public class ClusterDTO {

    private String name = "DEFAULT";

    private String appId;

    public ClusterDTO(String appId) {
        this.appId = appId;
    }
}
