package lucky.apollo.core.service.impl;

import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ItemDTO;
import lucky.apollo.common.exception.NotFoundException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.constant.OpAudit;
import lucky.apollo.core.entity.CommitPO;
import lucky.apollo.core.entity.ItemPO;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.service.AuditService;
import lucky.apollo.core.service.CommitService;
import lucky.apollo.core.service.ItemService;
import lucky.apollo.core.service.ItemSetService;
import lucky.apollo.core.utils.ConfigChangeContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Service
public class ItemSetServiceImpl implements ItemSetService {

    @Autowired
    private AuditService auditService;
    @Autowired
    private CommitService commitService;
    @Autowired
    private ItemService itemService;

    @Transactional
    @Override
    public ItemChangeSetsDTO updateSet(NamespacePO namespace, ItemChangeSetsDTO changeSets) {
        return updateSet(namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName(), changeSets);
    }

    @Transactional
    @Override
    public ItemChangeSetsDTO updateSet(String appId, String clusterName,
                                       String namespaceName, ItemChangeSetsDTO changeSet) {
        String operator = changeSet.getDataChangeLastModifiedBy();
        ConfigChangeContentBuilder configChangeContentBuilder = new ConfigChangeContentBuilder();

        if (!CollectionUtils.isEmpty(changeSet.getCreateItems())) {
            for (ItemDTO item : changeSet.getCreateItems()) {
                ItemPO entity = BeanUtils.transformWithIgnoreNull(ItemPO.class, item);
                entity.setDataChangeCreatedBy(operator);
                entity.setDataChangeLastModifiedBy(operator);
                ItemPO createdItem = itemService.save(entity);
                configChangeContentBuilder.createItem(createdItem);
            }
            auditService.audit("ItemSet", null, OpAudit.INSERT, operator);
        }

        if (!CollectionUtils.isEmpty(changeSet.getUpdateItems())) {
            for (ItemDTO item : changeSet.getUpdateItems()) {
                ItemPO entity = BeanUtils.transformWithIgnoreNull(ItemPO.class, item);

                ItemPO managedItem = itemService.findOne(entity.getId());
                if (managedItem == null) {
                    throw new NotFoundException(String.format("item not found.(key=%s)", entity.getKey()));
                }
                ItemPO beforeUpdateItem = BeanUtils.transformWithIgnoreNull(ItemPO.class, managedItem);

                //protect. only value,comment,lastModifiedBy,lineNum can be modified
                managedItem.setValue(entity.getValue());
                managedItem.setComment(entity.getComment());
                managedItem.setLineNum(entity.getLineNum());
                managedItem.setDataChangeLastModifiedBy(operator);

                ItemPO updatedItem = itemService.update(managedItem);
                configChangeContentBuilder.updateItem(beforeUpdateItem, updatedItem);

            }
            auditService.audit("ItemSet", null, OpAudit.UPDATE, operator);
        }

        if (!CollectionUtils.isEmpty(changeSet.getDeleteItems())) {
            for (ItemDTO item : changeSet.getDeleteItems()) {
                ItemPO deletedItem = itemService.delete(item.getId(), operator);
                configChangeContentBuilder.deleteItem(deletedItem);
            }
            auditService.audit("ItemSet", null, OpAudit.DELETE, operator);
        }

        if (configChangeContentBuilder.hasContent()) {
            createCommit(appId, clusterName, namespaceName, configChangeContentBuilder.build(),
                    changeSet.getDataChangeLastModifiedBy());
        }

        return changeSet;

    }

    private void createCommit(String appId, String clusterName, String namespaceName, String configChangeContent,
                              String operator) {

        CommitPO commit = new CommitPO();
        commit.setAppId(appId);
        commit.setClusterName(clusterName);
        commit.setNamespaceName(namespaceName);
        commit.setChangeSets(configChangeContent);
        commit.setDataChangeCreatedBy(operator);
        commit.setDataChangeLastModifiedBy(operator);
        commitService.save(commit);
    }
}
