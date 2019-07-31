package lucky.apollo.adminservice.aop;

import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ItemDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.core.config.ServiceConfig;
import lucky.apollo.core.entity.ItemPO;
import lucky.apollo.core.entity.NamespaceLockPO;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.service.ItemService;
import lucky.apollo.core.service.NamespaceLockService;
import lucky.apollo.core.service.NamespaceService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.service.spi.ServiceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@Aspect
@Component
@Slf4j
public class NamespaceAcquireLockAspect {

    private final NamespaceLockService namespaceLockService;
    private final NamespaceService namespaceService;
    private final ItemService itemService;
    private final ServiceConfig serviceConfig;

    public NamespaceAcquireLockAspect(
            final NamespaceLockService namespaceLockService,
            final NamespaceService namespaceService,
            final ItemService itemService,
            final ServiceConfig serviceConfig) {
        this.namespaceLockService = namespaceLockService;
        this.namespaceService = namespaceService;
        this.itemService = itemService;
        this.serviceConfig = serviceConfig;
    }


    //create item
    @Before("@annotation(lucky.apollo.adminservice.aop.PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, item, ..)")
    public void requireLockAdvice(String appId, String clusterName, String namespaceName,
                                  ItemDTO item) {
        acquireLock(appId, clusterName, namespaceName, item.getDataChangeLastModifiedBy());
    }

    //update item
    @Before("@annotation(lucky.apollo.adminservice.aop.PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, itemId, item, ..)")
    public void requireLockAdvice(String appId, String clusterName, String namespaceName, long itemId,
                                  ItemDTO item) {
        acquireLock(appId, clusterName, namespaceName, item.getDataChangeLastModifiedBy());
    }

    //update by change set
    @Before("@annotation(lucky.apollo.adminservice.aop.PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, changeSet, ..)")
    public void requireLockAdvice(String appId, String clusterName, String namespaceName,
                                  ItemChangeSetsDTO changeSet) {
        acquireLock(appId, clusterName, namespaceName, changeSet.getDataChangeLastModifiedBy());
    }

    //delete item
    @Before("@annotation(lucky.apollo.adminservice.aop.PreAcquireNamespaceLock) && args(itemId, operator, ..)")
    public void requireLockAdvice(long itemId, String operator) {
        ItemPO item = itemService.findOne(itemId);
        if (item == null) {
            throw new BadRequestException("item not exist.");
        }
        acquireLock(item.getNamespaceId(), operator);
    }

    void acquireLock(String appId, String clusterName, String namespaceName,
                     String currentUser) {

        NamespacePO namespace = namespaceService.findOne(appId, clusterName, namespaceName);

        acquireLock(namespace, currentUser);
    }

    void acquireLock(long namespaceId, String currentUser) {

        NamespacePO namespace = namespaceService.findOne(namespaceId);

        acquireLock(namespace, currentUser);

    }

    private void acquireLock(NamespacePO namespace, String currentUser) {
        if (namespace == null) {
            throw new BadRequestException("namespace not exist.");
        }

        long namespaceId = namespace.getId();

        NamespaceLockPO namespaceLock = namespaceLockService.findLock(namespaceId);
        if (namespaceLock == null) {
            try {
                tryLock(namespaceId, currentUser);
                //lock success
            } catch (DataIntegrityViolationException e) {
                //lock fail
                namespaceLock = namespaceLockService.findLock(namespaceId);
                checkLock(namespace, namespaceLock, currentUser);
            } catch (Exception e) {
                log.error("try lock error", e);
                throw e;
            }
        } else {
            //check lock owner is current user
            checkLock(namespace, namespaceLock, currentUser);
        }
    }

    private void tryLock(long namespaceId, String user) {
        NamespaceLockPO lock = new NamespaceLockPO();
        lock.setNamespaceId(namespaceId);
        lock.setDataChangeCreatedBy(user);
        lock.setDataChangeLastModifiedBy(user);
        namespaceLockService.tryLock(lock);
    }

    private void checkLock(NamespacePO namespace, NamespaceLockPO namespaceLock,
                           String currentUser) {
        if (namespaceLock == null) {
            throw new ServiceException(
                    String.format("Check lock for %s failed, please retry.", namespace.getNamespaceName()));
        }

        String lockOwner = namespaceLock.getDataChangeCreatedBy();
        if (!lockOwner.equals(currentUser)) {
            throw new BadRequestException(
                    "namespace:" + namespace.getNamespaceName() + " is modified by " + lockOwner);
        }
    }


}