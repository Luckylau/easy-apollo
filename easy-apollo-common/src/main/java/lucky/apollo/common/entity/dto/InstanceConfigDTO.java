package lucky.apollo.common.entity.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
@Data
public class InstanceConfigDTO {
    private ReleaseDTO release;
    private Date releaseDeliveryTime;
    private Date dataChangeLastModifiedTime;
}