package lucky.apollo.adminservice.aop;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import lucky.apollo.common.constant.GsonType;
import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ItemDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.core.config.ServiceConfig;
import lucky.apollo.core.entity.ItemPO;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.entity.ReleasePO;
import lucky.apollo.core.service.ItemService;
import lucky.apollo.core.service.NamespaceLockService;
import lucky.apollo.core.service.NamespaceService;
import lucky.apollo.core.service.ReleaseService;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * unlock namespace if is redo operation.
 * --------------------------------------------
 * For example: If namespace has a item K1 = v1
 * --------------------------------------------
 * First operate: change k1 = v2 (lock namespace)
 * Second operate: change k1 = v1 (unlock namespace)
 */
@Aspect
@Component
public class NamespaceUnlockAspect {

    private final NamespaceLockService namespaceLockService;
    private final NamespaceService namespaceService;
    private final ItemService itemService;
    private final ReleaseService releaseService;
    private final ServiceConfig serviceConfig;
    private Gson gson = new Gson();

    public NamespaceUnlockAspect(
            final NamespaceLockService namespaceLockService,
            final NamespaceService namespaceService,
            final ItemService itemService,
            final ReleaseService releaseService,
            final ServiceConfig serviceConfig) {
        this.namespaceLockService = namespaceLockService;
        this.namespaceService = namespaceService;
        this.itemService = itemService;
        this.releaseService = releaseService;
        this.serviceConfig = serviceConfig;
    }


    //create item
    @After("@annotation(lucky.apollo.adminservice.aop.PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, item, ..)")
    public void requireLockAdvice(String appId, String clusterName, String namespaceName,
                                  ItemDTO item) {
        tryUnlock(namespaceService.findOne(appId, clusterName, namespaceName));
    }

    //update item
    @After("@annotation(lucky.apollo.adminservice.aop.PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, itemId, item, ..)")
    public void requireLockAdvice(String appId, String clusterName, String namespaceName, long itemId,
                                  ItemDTO item) {
        tryUnlock(namespaceService.findOne(appId, clusterName, namespaceName));
    }

    //update by change set
    @After("@annotation(lucky.apollo.adminservice.aop.PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, changeSet, ..)")
    public void requireLockAdvice(String appId, String clusterName, String namespaceName,
                                  ItemChangeSetsDTO changeSet) {
        tryUnlock(namespaceService.findOne(appId, clusterName, namespaceName));
    }

    //delete item
    @After("@annotation(lucky.apollo.adminservice.aop.PreAcquireNamespaceLock) && args(itemId, operator, ..)")
    public void requireLockAdvice(long itemId, String operator) {
        ItemPO item = itemService.findOne(itemId);
        if (item == null) {
            throw new BadRequestException("item not exist.");
        }
        tryUnlock(namespaceService.findOne(item.getNamespaceId()));
    }

    private void tryUnlock(NamespacePO namespace) {

        if (!isModified(namespace)) {
            namespaceLockService.unlock(namespace.getId());
        }

    }

    boolean isModified(NamespacePO namespace) {
        ReleasePO release = releaseService.findLatestActiveRelease(namespace);
        List<ItemPO> items = itemService.findItemsWithoutOrdered(namespace.getId());

        if (release == null) {
            return hasNormalItems(items);
        }

        Map<String, String> releasedConfiguration = gson.fromJson(release.getConfigurations(), GsonType.CONFIG);
        Map<String, String> configurationFromItems = generateConfigurationFromItems(namespace, items);

        MapDifference<String, String> difference = Maps.difference(releasedConfiguration, configurationFromItems);

        return !difference.areEqual();

    }

    private boolean hasNormalItems(List<ItemPO> items) {
        for (ItemPO item : items) {
            if (!StringUtils.isEmpty(item.getKey())) {
                return true;
            }
        }

        return false;
    }

    private Map<String, String> generateConfigurationFromItems(NamespacePO namespace, List<ItemPO> namespaceItems) {

        return null;
    }

    private Map<String, String> generateMapFromItems(List<ItemPO> items, Map<String, String> configurationFromItems) {
        for (ItemPO item : items) {
            String key = item.getKey();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            configurationFromItems.put(key, item.getValue());
        }

        return configurationFromItems;
    }

}