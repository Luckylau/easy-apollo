package lucky.apollo.core.service.impl;

import lucky.apollo.core.entity.CommitPO;
import lucky.apollo.core.repository.CommitRepository;
import lucky.apollo.core.service.CommitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Service
public class CommitServiceImpl implements CommitService {

    @Autowired
    private CommitRepository commitRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommitPO save(CommitPO commit) {
        commit.setId(0);
        return commitRepository.save(commit);
    }

    @Override
    public List<CommitPO> find(String appId, String clusterName, String namespaceName, Pageable page) {
        return commitRepository.findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(appId, clusterName, namespaceName, page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchDelete(String appId, String clusterName, String namespaceName, String operator) {
        return commitRepository.batchDelete(appId, clusterName, namespaceName, operator);
    }
}
