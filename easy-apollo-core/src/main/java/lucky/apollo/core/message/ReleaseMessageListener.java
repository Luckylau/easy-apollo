package lucky.apollo.core.message;

import lucky.apollo.core.entity.ReleaseMessagePO;

/**
 * @Author luckylau
 * @Date 2019/12/4
 */
public interface ReleaseMessageListener {
    void handleMessage(ReleaseMessagePO message, String channel);
}
