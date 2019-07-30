package lucky.apollo.core.service;

import lucky.apollo.core.entity.NamespaceLockPO;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
public interface NamespaceLockService {
    NamespaceLockPO findLock(Long namespaceId);

    NamespaceLockPO tryLock(NamespaceLockPO lock);

    void unlock(Long namespaceId);
}
