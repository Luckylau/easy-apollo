package lucky.apollo.core.service.impl;

import lucky.apollo.core.entity.NamespaceLockPO;
import lucky.apollo.core.repository.NamespaceLockRepository;
import lucky.apollo.core.service.NamespaceLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@Service
public class NamespaceLockServiceImpl implements NamespaceLockService {

    @Autowired
    private NamespaceLockRepository namespaceLockRepository;

    @Override
    public NamespaceLockPO findLock(Long namespaceId) {
        return namespaceLockRepository.findByNamespaceId(namespaceId);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public NamespaceLockPO tryLock(NamespaceLockPO lock) {
        return namespaceLockRepository.save(lock);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unlock(Long namespaceId) {
        namespaceLockRepository.deleteByNamespaceId(namespaceId);
    }
}
