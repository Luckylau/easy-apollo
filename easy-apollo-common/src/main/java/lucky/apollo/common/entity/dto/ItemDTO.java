package lucky.apollo.common.entity.dto;

import lombok.Data;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
@Data
public class ItemDTO extends BaseDTO {

    private long id;

    private long namespaceId;

    private String key;

    private String value;

    private String comment;

    private int lineNum;
}
