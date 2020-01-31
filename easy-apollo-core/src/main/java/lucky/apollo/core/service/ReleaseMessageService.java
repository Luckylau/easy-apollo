package lucky.apollo.core.service;

import lucky.apollo.core.entity.ReleaseMessagePO;

import java.util.Collection;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/12/6
 */
public interface ReleaseMessageService {

    List<ReleaseMessagePO> findLatestReleaseMessagesGroupByMessages(Collection<String> messages);

    ReleaseMessagePO findLatestReleaseMessageForMessages(Collection<String> messages);

}
