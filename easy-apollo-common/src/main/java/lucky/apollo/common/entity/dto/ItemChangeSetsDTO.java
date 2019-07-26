package lucky.apollo.common.entity.dto;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
@Data
public class ItemChangeSetsDTO extends BaseDTO {

    private List<ItemDTO> createItems = new LinkedList<>();
    private List<ItemDTO> updateItems = new LinkedList<>();
    private List<ItemDTO> deleteItems = new LinkedList<>();
}