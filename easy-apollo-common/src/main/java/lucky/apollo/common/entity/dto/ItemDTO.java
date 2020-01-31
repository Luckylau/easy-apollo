package lucky.apollo.common.entity.dto;

import lombok.Data;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
@Data
public class ItemDTO extends BaseDTO {

    private Long id;

    private Long namespaceId;

    private String key;

    private String value;

    private String comment;

    private Integer lineNum;

    public ItemDTO() {
    }

    public ItemDTO(String key, String value, String comment, int lineNum) {
        this.key = key;
        this.value = value;
        this.comment = comment;
        this.lineNum = lineNum;
    }
}
