package lucky.apollo.portal.entity.model;

import lombok.Data;
import lucky.apollo.common.entity.dto.NamespaceDTO;


/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Data
public class NamespaceCreationModel {
    private String env = "default";

    private NamespaceDTO namespace;
}