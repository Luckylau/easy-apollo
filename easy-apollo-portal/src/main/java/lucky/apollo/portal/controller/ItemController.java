package lucky.apollo.portal.controller;

import lucky.apollo.common.entity.dto.ItemDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.utils.StringUtils;
import lucky.apollo.portal.entity.model.NamespaceTextModel;
import lucky.apollo.portal.resolver.PermissionValidator;
import lucky.apollo.portal.service.ItemService;
import lucky.apollo.portal.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static lucky.apollo.common.utils.RequestPrecondition.checkModel;

/**
 * @Author luckylau
 * @Date 2019/9/20
 */
@RestController
public class ItemController {
    private final ItemService itemService;
    private final UserService userService;
    private final PermissionValidator permissionValidator;

    public ItemController(final ItemService itemService, final UserService userService, final PermissionValidator permissionValidator) {
        this.itemService = itemService;
        this.userService = userService;
        this.permissionValidator = permissionValidator;
    }

    @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
    @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items", method = RequestMethod.PUT, consumes = {
            "application/json"})
    public void modifyItemsByText(@PathVariable String appId, @PathVariable String env,
                                  @PathVariable String clusterName, @PathVariable String namespaceName,
                                  @RequestBody NamespaceTextModel model) {

        checkModel(model != null);

        model.setAppId(appId);
        model.setClusterName(clusterName);
        model.setEnv(env);
        model.setNamespaceName(namespaceName);

        itemService.updateConfigItemByText(model);
    }

    @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
    @PostMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/item")
    public ItemDTO createItem(@PathVariable String appId, @PathVariable String env,
                              @PathVariable String clusterName, @PathVariable String namespaceName,
                              @RequestBody ItemDTO item) {
        checkModel(isValidItem(item));

        //protect
        item.setLineNum(0);
        item.setId(0L);
        String userId = userService.getCurrentUser().getUserId();
        item.setDataChangeCreatedBy(userId);
        item.setDataChangeLastModifiedBy(userId);
        item.setDataChangeCreatedTime(null);
        item.setDataChangeLastModifiedTime(null);

        return itemService.createItem(appId, namespaceName, clusterName, item);
    }

    private boolean isValidItem(ItemDTO item) {
        return Objects.nonNull(item) && !StringUtils.isContainEmpty(item.getKey());
    }

    @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
    @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/item", method = RequestMethod.PUT)
    public void updateItem(@PathVariable String appId, @PathVariable String env,
                           @PathVariable String clusterName, @PathVariable String namespaceName,
                           @RequestBody ItemDTO item) {
        checkModel(isValidItem(item));

        String username = userService.getCurrentUser().getUserId();
        item.setDataChangeLastModifiedBy(username);

        itemService.updateItem(appId, namespaceName, clusterName, item);
    }

    @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName) ")
    @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}", method = RequestMethod.DELETE)
    public void deleteItem(@PathVariable String appId, @PathVariable String env,
                           @PathVariable String clusterName, @PathVariable String namespaceName,
                           @PathVariable long itemId) {
        if (itemId <= 0) {
            throw new BadRequestException("item id invalid");
        }
        itemService.deleteItem(itemId, userService.getCurrentUser().getUserId());
    }


}
