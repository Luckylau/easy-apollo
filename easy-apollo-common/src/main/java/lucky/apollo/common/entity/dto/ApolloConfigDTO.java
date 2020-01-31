package lucky.apollo.common.entity.dto;

import lombok.Data;

import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/12/7
 */
@Data
public class ApolloConfigDTO {
    private String appId;

    private String cluster;

    private String namespaceName;

    private Map<String, String> configurations;

    private String releaseKey;

    public ApolloConfigDTO() {
    }

    public ApolloConfigDTO(String appId,
                           String cluster,
                           String namespaceName,
                           String releaseKey) {
        this.appId = appId;
        this.cluster = cluster;
        this.namespaceName = namespaceName;
        this.releaseKey = releaseKey;
    }

}
