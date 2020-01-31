package lucky.apollo.client.constant;

/**
 * @Author liuJun
 * @Date 2019/12/13
 */
public enum ConfigSourceType {
    /**
     * 远端
     */
    REMOTE("Loaded from remote config service"),
    /**
     * 本地
     */
    LOCAL("Loaded from local cache"),
    /**
     * 失败
     */
    NONE("Load failed");

    private final String description;

    ConfigSourceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
