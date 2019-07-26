package lucky.apollo.portal.listener;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Data
public class ConfigPublishEvent extends ApplicationEvent {

    private ConfigPublishInfo configPublishInfo;

    private ConfigPublishEvent(Object source) {
        super(source);
        configPublishInfo = (ConfigPublishInfo) source;
    }

    public static ConfigPublishEvent instance() {
        ConfigPublishInfo info = new ConfigPublishInfo();
        return new ConfigPublishEvent(info);
    }

    public ConfigPublishEvent withAppId(String appId) {
        configPublishInfo.setAppId(appId);
        return this;
    }

    public ConfigPublishEvent withNamespace(String namespaceName) {
        configPublishInfo.setNamespaceName(namespaceName);
        return this;
    }

    public ConfigPublishEvent withReleaseId(long releaseId) {
        configPublishInfo.setReleaseId(releaseId);
        return this;
    }

    public ConfigPublishEvent withPreviousReleaseId(long previousReleaseId) {
        configPublishInfo.setPreviousReleaseId(previousReleaseId);
        return this;
    }

    public ConfigPublishEvent setNormalPublishEvent(boolean isNormalPublishEvent) {
        configPublishInfo.setNormalPublishEvent(isNormalPublishEvent);
        return this;
    }

    public ConfigPublishEvent setGrayPublishEvent(boolean isGrayPublishEvent) {
        configPublishInfo.setGrayPublishEvent(isGrayPublishEvent);
        return this;
    }

    public ConfigPublishEvent setRollbackEvent(boolean isRollbackEvent) {
        configPublishInfo.setRollbackEvent(isRollbackEvent);
        return this;
    }

    public ConfigPublishEvent setMergeEvent(boolean isMergeEvent) {
        configPublishInfo.setMergeEvent(isMergeEvent);
        return this;
    }

    @Data
    private static class ConfigPublishInfo {
        private String appId;
        private String namespaceName;
        private long releaseId;
        private long previousReleaseId;
        private boolean isRollbackEvent;
        private boolean isMergeEvent;
        private boolean isNormalPublishEvent;
        private boolean isGrayPublishEvent;
    }
}