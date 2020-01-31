package lucky.apollo.core.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.core.entity.ReleaseMessagePO;
import lucky.apollo.core.repository.ReleaseMessageRepository;
import lucky.apollo.core.service.ReleaseMessageService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/12/6
 */
@Service
@Slf4j
public class ReleaseMessageServiceImpl implements ReleaseMessageService {

    private final ReleaseMessageRepository releaseMessageRepository;

    public ReleaseMessageServiceImpl(final ReleaseMessageRepository releaseMessageRepository) {
        this.releaseMessageRepository = releaseMessageRepository;
    }


    @Override
    public List<ReleaseMessagePO> findLatestReleaseMessagesGroupByMessages(Collection<String> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }
        List<Object[]> result =
                releaseMessageRepository.findLatestReleaseMessagesGroupByMessages(messages);
        List<ReleaseMessagePO> releaseMessages = Lists.newArrayList();
        for (Object[] o : result) {
            try {
                ReleaseMessagePO releaseMessage = new ReleaseMessagePO((String) o[0]);
                releaseMessage.setId((Long) o[1]);
                releaseMessages.add(releaseMessage);
            } catch (Exception ex) {
                log.error("Parsing LatestReleaseMessagesGroupByMessages failed", ex);
            }
        }
        return releaseMessages;
    }

    @Override
    public ReleaseMessagePO findLatestReleaseMessageForMessages(Collection<String> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return null;
        }
        return releaseMessageRepository.findTopByMessageInOrderByIdDesc(messages);
    }
}
