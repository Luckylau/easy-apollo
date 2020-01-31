package lucky.apollo.portal.entity.model;

import lombok.Data;
import lucky.apollo.common.constant.ConfigFileFormat;
import lucky.apollo.common.utils.StringUtils;

/**
 * @Author luckylau
 * @Date 2019/9/25
 */
@Data
public class NamespaceTextModel implements Verifiable {

    private String appId;
    private String env;
    private String clusterName;
    private String namespaceName;
    private int namespaceId;
    private String format;
    private String configText;

    @Override
    public boolean isInvalid() {
        return StringUtils.isContainEmpty(appId, env, clusterName, namespaceName) || namespaceId <= 0;
    }

    public ConfigFileFormat getFormat() {
        return ConfigFileFormat.fromString(this.format);
    }

}
