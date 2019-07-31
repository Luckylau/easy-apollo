package lucky.apollo.adminservice.controller;

import lucky.apollo.adminservice.aop.PreAcquireNamespaceLock;
import lucky.apollo.common.entity.dto.ItemDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.exception.NotFoundException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.entity.CommitPO;
import lucky.apollo.core.entity.ItemPO;
import lucky.apollo.core.entity.NamespacePO;
import lucky.apollo.core.service.CommitService;
import lucky.apollo.core.service.ItemService;
import lucky.apollo.core.service.NamespaceService;
import lucky.apollo.core.utils.ConfigChangeContentBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@RestController
public class ItemController {

    private final ItemService itemService;
    private final NamespaceService namespaceService;
    private final CommitService commitService;

    public ItemController(final ItemService itemService, final NamespaceService namespaceService, final CommitService commitService) {
        this.itemService = itemService;
        this.namespaceService = namespaceService;
        this.commitService = commitService;
    }

    @PreAcquireNamespaceLock
    @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items")
    public ItemDTO create(@PathVariable("appId") String appId,
                          @PathVariable("clusterName") String clusterName,
                          @PathVariable("namespaceName") String namespaceName, @RequestBody ItemDTO dto) {
        ItemPO entity = BeanUtils.transformWithIgnoreNull(ItemPO.class, dto);

        ConfigChangeContentBuilder builder = new ConfigChangeContentBuilder();
        ItemPO managedEntity = itemService.findOne(appId, clusterName, namespaceName, entity.getKey());
        if (managedEntity != null) {
            throw new BadRequestException("item already exists");
        } else {
            entity = itemService.save(entity);
            builder.createItem(entity);
        }
        dto = BeanUtils.transformWithIgnoreNull(ItemDTO.class, entity);

        CommitPO commit = new CommitPO();
        commit.setAppId(appId);
        commit.setClusterName(clusterName);
        commit.setNamespaceName(namespaceName);
        commit.setChangeSets(builder.build());
        commit.setDataChangeCreatedBy(dto.getDataChangeLastModifiedBy());
        commit.setDataChangeLastModifiedBy(dto.getDataChangeLastModifiedBy());
        commitService.save(commit);

        return dto;
    }

    @PreAcquireNamespaceLock
    @PutMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}")
    public ItemDTO update(@PathVariable("appId") String appId,
                          @PathVariable("clusterName") String clusterName,
                          @PathVariable("namespaceName") String namespaceName,
                          @PathVariable("itemId") long itemId,
                          @RequestBody ItemDTO itemDTO) {

        ItemPO entity = BeanUtils.transformWithIgnoreNull(ItemPO.class, itemDTO);

        ConfigChangeContentBuilder builder = new ConfigChangeContentBuilder();

        ItemPO managedEntity = itemService.findOne(itemId);
        if (managedEntity == null) {
            throw new BadRequestException("item not exist");
        }

        ItemPO beforeUpdateItem = BeanUtils.transformWithIgnoreNull(ItemPO.class, managedEntity);

        //protect. only value,comment,lastModifiedBy can be modified
        managedEntity.setValue(entity.getValue());
        managedEntity.setComment(entity.getComment());
        managedEntity.setDataChangeLastModifiedBy(entity.getDataChangeLastModifiedBy());

        entity = itemService.update(managedEntity);
        builder.updateItem(beforeUpdateItem, entity);
        itemDTO = BeanUtils.transformWithIgnoreNull(ItemDTO.class, entity);

        if (builder.hasContent()) {
            CommitPO commit = new CommitPO();
            commit.setAppId(appId);
            commit.setClusterName(clusterName);
            commit.setNamespaceName(namespaceName);
            commit.setChangeSets(builder.build());
            commit.setDataChangeCreatedBy(itemDTO.getDataChangeLastModifiedBy());
            commit.setDataChangeLastModifiedBy(itemDTO.getDataChangeLastModifiedBy());
            commitService.save(commit);
        }

        return itemDTO;
    }

    @PreAcquireNamespaceLock
    @DeleteMapping("/items/{itemId}")
    public void delete(@PathVariable("itemId") long itemId, @RequestParam String operator) {
        ItemPO entity = itemService.findOne(itemId);
        if (entity == null) {
            throw new NotFoundException("item not found for itemId " + itemId);
        }
        itemService.delete(entity.getId(), operator);

        NamespacePO namespace = namespaceService.findOne(entity.getNamespaceId());

        CommitPO commit = new CommitPO();
        commit.setAppId(namespace.getAppId());
        commit.setClusterName(namespace.getClusterName());
        commit.setNamespaceName(namespace.getNamespaceName());
        commit.setChangeSets(new ConfigChangeContentBuilder().deleteItem(entity).build());
        commit.setDataChangeCreatedBy(operator);
        commit.setDataChangeLastModifiedBy(operator);
        commitService.save(commit);
    }

    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items")
    public List<ItemDTO> findItems(@PathVariable("appId") String appId,
                                   @PathVariable("clusterName") String clusterName,
                                   @PathVariable("namespaceName") String namespaceName) {
        return BeanUtils.batchTransformWithIgnoreNull(ItemDTO.class, itemService.findItemsWithOrdered(appId, clusterName, namespaceName));
    }

    @GetMapping("/items/{itemId}")
    public ItemDTO get(@PathVariable("itemId") long itemId) {
        ItemPO item = itemService.findOne(itemId);
        if (item == null) {
            throw new NotFoundException("item not found for itemId " + itemId);
        }
        return BeanUtils.transformWithIgnoreNull(ItemDTO.class, item);
    }

    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{key:.+}")
    public ItemDTO get(@PathVariable("appId") String appId,
                       @PathVariable("clusterName") String clusterName,
                       @PathVariable("namespaceName") String namespaceName, @PathVariable("key") String key) {
        ItemPO item = itemService.findOne(appId, clusterName, namespaceName, key);
        if (item == null) {
            throw new NotFoundException(
                    String.format("item not found for %s %s %s %s", appId, clusterName, namespaceName, key));
        }
        return BeanUtils.transformWithIgnoreNull(ItemDTO.class, item);
    }
}
