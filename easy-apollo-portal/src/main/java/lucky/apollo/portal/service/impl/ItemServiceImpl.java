package lucky.apollo.portal.service.impl;

import lucky.apollo.common.constant.ConfigFileFormat;
import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.common.entity.dto.ItemDTO;
import lucky.apollo.common.entity.dto.NamespaceDTO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.portal.adminsevice.api.AdminServiceApi;
import lucky.apollo.portal.component.txtresolver.ConfigTextResolver;
import lucky.apollo.portal.entity.model.NamespaceTextModel;
import lucky.apollo.portal.service.ItemService;
import lucky.apollo.portal.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/9/20
 */
@Service
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final AdminServiceApi adminServiceAPi;
    private final ConfigTextResolver fileTextResolver;
    private final ConfigTextResolver propertyResolver;

    public ItemServiceImpl(
            final UserService userService,
            final AdminServiceApi adminServiceAPi,
            final @Qualifier("fileTextResolver") ConfigTextResolver fileTextResolver,
            final @Qualifier("propertyResolver") ConfigTextResolver propertyResolver) {
        this.userService = userService;
        this.adminServiceAPi = adminServiceAPi;
        this.fileTextResolver = fileTextResolver;
        this.propertyResolver = propertyResolver;
    }


    @Override
    public ItemDTO createItem(String appId, String namespaceName, String cluster, ItemDTO item) {
        NamespaceDTO namespaceDTO = adminServiceAPi.loadNamespace(appId, namespaceName, cluster);
        if (namespaceDTO == null) {
            throw new BadRequestException(
                    "namespace:" + namespaceName + " not exist ");
        }
        item.setNamespaceId(namespaceDTO.getId());

        return adminServiceAPi.createItem(appId, namespaceName, cluster, item);
    }

    @Override
    public void updateConfigItemByText(NamespaceTextModel model) {
        String appId = model.getAppId();
        String namespaceName = model.getNamespaceName();
        String cluster = model.getClusterName();
        long namespaceId = model.getNamespaceId();
        String configText = model.getConfigText();
        ConfigTextResolver resolver =
                model.getFormat() == ConfigFileFormat.Properties ? propertyResolver : fileTextResolver;

        ItemChangeSetsDTO changeSets = resolver.resolve(namespaceId, configText,
                adminServiceAPi.findItems(appId, cluster, namespaceName));
        if (changeSets.isEmpty()) {
            return;
        }

        changeSets.setDataChangeLastModifiedBy(userService.getCurrentUser().getUserId());
        updateItems(appId, namespaceName, cluster, changeSets);
    }

    @Override
    public void updateItems(String appId, String namespaceName, String cluster, ItemChangeSetsDTO changeSets) {
        adminServiceAPi.updateItemsByChangeSet(appId, namespaceName, cluster, changeSets);
    }

    @Override
    public void updateItem(String appId, String namespaceName, String cluster, ItemDTO item) {
        adminServiceAPi.updateItem(appId, namespaceName, cluster, item.getId(), item);
    }

    @Override
    public void deleteItem(long itemId, String userId) {
        adminServiceAPi.deleteItem(itemId, userId);
    }

    @Override
    public List<ItemDTO> findItems(String appId, String namespaceName, String cluster) {
        return adminServiceAPi.findItems(appId, namespaceName, cluster);
    }

}
