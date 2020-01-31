package lucky.apollo.common.entity.dto;

/**
 * @Author luckylau
 * @Date 2019/12/9
 */
public class ApolloConfigNotificationDTO {
    private String namespaceName;
    private long notificationId;
    private volatile ApolloNotificationMessageDTO messages;

    /**
     * for json converter
     */
    public ApolloConfigNotificationDTO() {
    }

    public ApolloConfigNotificationDTO(String namespaceName, long notificationId) {
        this.namespaceName = namespaceName;
        this.notificationId = notificationId;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public long getNotificationId() {
        return notificationId;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public ApolloNotificationMessageDTO getMessages() {
        return messages;
    }

    public void setMessages(ApolloNotificationMessageDTO messages) {
        this.messages = messages;
    }

    public void addMessage(String key, long notificationId) {
        if (this.messages == null) {
            synchronized (this) {
                if (this.messages == null) {
                    this.messages = new ApolloNotificationMessageDTO();
                }
            }
        }
        this.messages.put(key, notificationId);
    }

    @Override
    public String toString() {
        return "ApolloConfigNotificationDTO{" +
                "namespaceName='" + namespaceName + '\'' +
                ", notificationId=" + notificationId +
                '}';
    }
}
