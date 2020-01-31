package lucky.apollo.portal.service.impl;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.constant.GsonType;
import lucky.apollo.common.entity.dto.ItemDTO;
import lucky.apollo.common.entity.dto.NamespaceDTO;
import lucky.apollo.common.entity.dto.ReleaseDTO;
import lucky.apollo.common.entity.po.AppNamespacePO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.portal.adminsevice.api.AdminServiceApi;
import lucky.apollo.portal.constant.RoleType;
import lucky.apollo.portal.entity.bo.ItemInfo;
import lucky.apollo.portal.entity.bo.NamespaceInfo;
import lucky.apollo.portal.service.*;
import lucky.apollo.portal.utils.RoleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
@Service
@Slf4j
public class NamespaceServiceImpl implements NamespaceService {

    private Gson gson = new Gson();

    @Autowired
    private UserService userService;

    @Autowired
    private AdminServiceApi adminServiceApi;

    @Autowired
    private AppNamespaceService appNamespaceService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Lazy
    @Autowired
    private NamespaceBranchService namespaceBranchService;


    @Override
    public NamespaceDTO createNamespace(NamespaceDTO namespace) {
        String operator = userService.getCurrentUser().getUserId();
        if (StringUtils.isEmpty(namespace.getDataChangeCreatedBy())) {
            namespace.setDataChangeCreatedBy(operator);
        }
        namespace.setDataChangeLastModifiedBy(operator);
        NamespaceDTO createdNamespace = adminServiceApi.createNamespace(namespace);
        return createdNamespace;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNamespace(String appId, String clusterName, String namespaceName) {

        if (namespaceHasInstances(appId, clusterName, namespaceName)) {
            throw new BadRequestException(
                    "Can not delete namespace because namespace has active instances");
        }

        //2. check child namespace has not instances
        NamespaceDTO childNamespace = namespaceBranchService
                .findBranchBaseInfo(appId, clusterName, namespaceName);
        if (childNamespace != null &&
                namespaceHasInstances(appId, clusterName, namespaceName)) {
            throw new BadRequestException(
                    "Can not delete namespace because namespace's branch has active instances");
        }

        String operator = userService.getCurrentUser().getUserId();

        adminServiceApi.deleteNamespace(appId, namespaceName, operator);
    }

    @Override
    public NamespaceDTO loadNamespaceBaseInfo(String appId, String clusterName, String namespaceName) {
        NamespaceDTO namespace = adminServiceApi.loadNamespace(appId, namespaceName, clusterName);
        if (namespace == null) {
            throw new BadRequestException("namespaces not exist");
        }
        return namespace;
    }

    /**
     * load cluster all namespace info with items
     */
    @Override
    public List<NamespaceInfo> findNamespaceBOs(String appId, String cluster) {

        List<NamespaceDTO> namespaces = adminServiceApi.findNamespace(appId, cluster);
        if (namespaces == null || namespaces.size() == 0) {
            throw new BadRequestException("namespaces not exist");
        }

        List<NamespaceInfo> namespaceInfos = new LinkedList<>();
        for (NamespaceDTO namespace : namespaces) {

            NamespaceInfo namespaceInfo;
            try {
                namespaceInfo = transformNamespace2BO(namespace);
                namespaceInfos.add(namespaceInfo);
            } catch (Exception e) {
                log.error("parse namespace error. app id:{}, namespace:{}",
                        appId, namespace.getNamespaceName(), e);
                throw e;
            }
        }

        return namespaceInfos;
    }

    @Override
    public List<NamespaceDTO> findNamespaces(String appId, String cluster) {
        return adminServiceApi.findNamespace(appId, cluster);
    }

    @Override
    public NamespaceInfo loadNamespaceBO(String appId, String clusterName,
                                         String namespaceName) {
        NamespaceDTO namespace = adminServiceApi.loadNamespace(appId, namespaceName, clusterName);
        if (namespace == null) {
            throw new BadRequestException("namespaces not exist");
        }
        return transformNamespace2BO(namespace);
    }

    @Override
    public boolean namespaceHasInstances(String appId, String clusterName, String namespaceName) {
        return adminServiceApi.getInstanceCountByNamespace(appId, namespaceName) > 0;
    }


    private NamespaceInfo transformNamespace2BO(NamespaceDTO namespace) {
        NamespaceInfo namespaceInfo = new NamespaceInfo();
        namespaceInfo.setBaseInfo(namespace);

        String appId = namespace.getAppId();
        String namespaceName = namespace.getNamespaceName();
        String cluster = namespace.getClusterName();

        fillAppNamespaceProperties(namespaceInfo);

        List<ItemInfo> itemInfos = new LinkedList<>();
        namespaceInfo.setItems(itemInfos);

        //latest Release
        ReleaseDTO latestRelease;
        Map<String, String> releaseItems = new HashMap<>();
        latestRelease = adminServiceApi.loadLatestRelease(appId, namespaceName, cluster);
        if (latestRelease != null) {
            releaseItems = gson.fromJson(latestRelease.getConfigurations(), GsonType.CONFIG);
        }

        //not Release config items
        List<ItemDTO> items = adminServiceApi.findItems(appId, cluster, namespaceName);
        int modifiedItemCnt = 0;
        for (ItemDTO itemDTO : items) {

            ItemInfo itemInfo = transformItem2BO(itemDTO, releaseItems);

            if (itemInfo.isModified()) {
                modifiedItemCnt++;
            }

            itemInfos.add(itemInfo);
        }

        //deleted items
        List<ItemInfo> deletedItems = parseDeletedItems(items, releaseItems);
        itemInfos.addAll(deletedItems);
        modifiedItemCnt += deletedItems.size();

        namespaceInfo.setItemModifiedCnt(modifiedItemCnt);

        return namespaceInfo;
    }

    private void fillAppNamespaceProperties(NamespaceInfo namespace) {

        NamespaceDTO namespaceDTO = namespace.getBaseInfo();
        //先从当前appId下面找,
        AppNamespacePO appNamespace =
                appNamespaceService
                        .findByAppIdAndName(namespaceDTO.getAppId(), namespaceDTO.getNamespaceName());
        namespace.setParentAppId(appNamespace.getAppId());
        namespace.setComment(appNamespace.getComment());
        namespace.setFormat(appNamespace.getFormat());
    }

    private List<ItemInfo> parseDeletedItems(List<ItemDTO> newItems, Map<String, String> releaseItems) {
        Map<String, ItemDTO> newItemMap = BeanUtils.mapByKey("key", newItems);

        List<ItemInfo> deletedItems = new LinkedList<>();
        for (Map.Entry<String, String> entry : releaseItems.entrySet()) {
            String key = entry.getKey();
            if (newItemMap.get(key) == null) {
                ItemInfo deletedItem = new ItemInfo();

                deletedItem.setDeleted(true);
                ItemDTO deletedItemDto = new ItemDTO();
                deletedItemDto.setKey(key);
                String oldValue = entry.getValue();
                deletedItem.setItem(deletedItemDto);

                deletedItemDto.setValue(oldValue);
                deletedItem.setModified(true);
                deletedItem.setOldValue(oldValue);
                deletedItem.setNewValue("");
                deletedItems.add(deletedItem);
            }
        }
        return deletedItems;
    }

    private ItemInfo transformItem2BO(ItemDTO itemDTO, Map<String, String> releaseItems) {
        String key = itemDTO.getKey();
        ItemInfo itemInfo = new ItemInfo();
        itemInfo.setItem(itemDTO);
        String newValue = itemDTO.getValue();
        String oldValue = releaseItems.get(key);
        //new item or modified
        boolean oldValueIsNullOrNotEqNewValue = StringUtils.isEmpty(oldValue) || !newValue.equals(oldValue);
        if (!StringUtils.isEmpty(key) && oldValueIsNullOrNotEqNewValue) {
            itemInfo.setModified(true);
            itemInfo.setOldValue(oldValue == null ? "" : oldValue);
            itemInfo.setNewValue(newValue);
        }
        return itemInfo;
    }

    @Override
    public void assignNamespaceRoleToOperator(String appId, String namespaceName, String operator) {
        //default assign modify、release namespace role to namespace creator

        rolePermissionService
                .assignRoleToUsers(
                        RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.MODIFY_NAMESPACE),
                        Sets.newHashSet(operator), operator);
        rolePermissionService
                .assignRoleToUsers(
                        RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.RELEASE_NAMESPACE),
                        Sets.newHashSet(operator), operator);
    }
}