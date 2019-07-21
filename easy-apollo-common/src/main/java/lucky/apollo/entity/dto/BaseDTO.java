package lucky.apollo.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
@Setter
@Getter
public class BaseDTO {
    protected String dataChangeCreatedBy;

    protected String dataChangeLastModifiedBy;

    protected Date dataChangeCreatedTime;

    protected Date dataChangeLastModifiedTime;
}