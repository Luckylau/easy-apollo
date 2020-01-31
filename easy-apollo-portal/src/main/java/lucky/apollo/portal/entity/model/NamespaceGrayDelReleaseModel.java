package lucky.apollo.portal.entity.model;

import lombok.Data;

import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/9/23
 */
@Data
public class NamespaceGrayDelReleaseModel extends NamespaceReleaseModel implements Verifiable {
    private Set<String> grayDelKeys;
}
