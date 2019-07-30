package lucky.apollo.core.service.impl;

import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.exception.NotFoundException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.config.ServiceConfig;
import lucky.apollo.core.constant.OpAudit;
import lucky.apollo.core.entity.ItemPO;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.repository.ItemRepository;
import lucky.apollo.core.service.AuditService;
import lucky.apollo.core.service.ItemService;
import lucky.apollo.core.service.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private AuditService auditService;

    @Autowired
    private ServiceConfig serviceConfig;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ItemPO delete(long id, String operator) {
        ItemPO item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            throw new IllegalArgumentException("item not exist. ID:" + id);
        }

        item.setDeleted(true);
        item.setDataChangeLastModifiedBy(operator);
        ItemPO deletedItem = itemRepository.save(item);

        auditService.audit(ItemPO.class.getSimpleName(), id, OpAudit.DELETE, operator);
        return deletedItem;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchDelete(long namespaceId, String operator) {
        return itemRepository.deleteByNamespaceId(namespaceId, operator);

    }

    @Override
    public ItemPO findOne(String appId, String clusterName, String namespaceName, String key) {
        NamespacePO namespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (namespace == null) {
            throw new NotFoundException(
                    String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
        }
        return itemRepository.findByNamespaceIdAndKey(namespace.getId(), key);
    }

    @Override
    public ItemPO findLastOne(String appId, String clusterName, String namespaceName) {
        NamespacePO namespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (namespace == null) {
            throw new NotFoundException(
                    String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
        }
        return findLastOne(namespace.getId());
    }

    @Override
    public ItemPO findLastOne(long namespaceId) {
        return itemRepository.findFirst1ByNamespaceIdOrderByLineNumDesc(namespaceId);
    }

    @Override
    public ItemPO findOne(long itemId) {
        ItemPO item = itemRepository.findById(itemId).orElse(null);
        return item;
    }

    @Override
    public List<ItemPO> findItemsWithoutOrdered(Long namespaceId) {
        List<ItemPO> items = itemRepository.findByNamespaceId(namespaceId);
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }

    @Override
    public List<ItemPO> findItemsWithoutOrdered(String appId, String clusterName, String namespaceName) {
        NamespacePO namespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (namespace != null) {
            return findItemsWithoutOrdered(namespace.getId());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ItemPO> findItemsWithOrdered(Long namespaceId) {
        List<ItemPO> items = itemRepository.findByNamespaceIdOrderByLineNumAsc(namespaceId);
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }

    @Override
    public List<ItemPO> findItemsWithOrdered(String appId, String clusterName, String namespaceName) {
        NamespacePO namespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (namespace != null) {
            return findItemsWithOrdered(namespace.getId());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ItemPO> findItemsModifiedAfterDate(long namespaceId, Date date) {
        return itemRepository.findByNamespaceIdAndDataChangeLastModifiedTimeGreaterThan(namespaceId, date);
    }

    @Transactional
    @Override
    public ItemPO save(ItemPO entity) {
        checkItemKeyLength(entity.getKey());
        checkItemValueLength(entity.getNamespaceId(), entity.getValue());
        //protection
        entity.setId(0);

        if (entity.getLineNum() == 0) {
            ItemPO lastItem = findLastOne(entity.getNamespaceId());
            int lineNum = lastItem == null ? 1 : lastItem.getLineNum() + 1;
            entity.setLineNum(lineNum);
        }

        ItemPO item = itemRepository.save(entity);

        auditService.audit(ItemPO.class.getSimpleName(), item.getId(), OpAudit.INSERT,
                item.getDataChangeCreatedBy());

        return item;
    }

    @Transactional
    @Override
    public ItemPO update(ItemPO item) {
        checkItemValueLength(item.getNamespaceId(), item.getValue());
        ItemPO managedItem = itemRepository.findById(item.getId()).orElse(null);
        BeanUtils.copyPropertiesWithIgnore(item, managedItem);
        managedItem = itemRepository.save(managedItem);

        auditService.audit(ItemPO.class.getSimpleName(), managedItem.getId(), OpAudit.UPDATE,
                managedItem.getDataChangeLastModifiedBy());

        return managedItem;
    }

    private boolean checkItemValueLength(long namespaceId, String value) {
        int limit = getItemValueLengthLimit(namespaceId);
        if (!StringUtils.isEmpty(value) && value.length() > limit) {
            throw new BadRequestException("value too long. length limit:" + limit);
        }
        return true;
    }

    private boolean checkItemKeyLength(String key) {
        return true;
    }

    private int getItemValueLengthLimit(long namespaceId) {
        return 0;
    }
}
