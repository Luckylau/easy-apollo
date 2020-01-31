package lucky.apollo.common.entity.dto;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/12/4
 */

public class ApolloNotificationMessageDTO {
    private Map<String, Long> details;

    public ApolloNotificationMessageDTO() {
        this(Maps.newHashMap());
    }

    private ApolloNotificationMessageDTO(Map<String, Long> details) {
        this.details = details;
    }

    public void put(String key, long notificationId) {
        details.put(key, notificationId);
    }

    public Long get(String key) {
        return this.details.get(key);
    }

    public boolean has(String key) {
        return this.details.containsKey(key);
    }

    public boolean isEmpty() {
        return this.details.isEmpty();
    }

    public Map<String, Long> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Long> details) {
        this.details = details;
    }

    public void mergeFrom(ApolloNotificationMessageDTO source) {
        if (source == null) {
            return;
        }

        for (Map.Entry<String, Long> entry : source.getDetails().entrySet()) {
            //to make sure the notification id always grows bigger
            if (this.has(entry.getKey()) &&
                    this.get(entry.getKey()) >= entry.getValue()) {
                continue;
            }
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public ApolloNotificationMessageDTO clone() {
        return new ApolloNotificationMessageDTO(ImmutableMap.copyOf(this.details));
    }
}
