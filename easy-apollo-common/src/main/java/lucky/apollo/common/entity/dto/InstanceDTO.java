package lucky.apollo.common.entity.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
@Data
public class InstanceDTO {
    private long id;

    private String appId;

    private String clusterName;

    private String dataCenter;

    private String ip;

    private List<lucky.apollo.common.entity.dto.InstanceConfigDTO> configs;

    private Date dataChangeCreatedTime;
}