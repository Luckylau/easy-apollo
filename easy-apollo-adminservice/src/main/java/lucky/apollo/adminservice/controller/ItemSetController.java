package lucky.apollo.adminservice.controller;

import lucky.apollo.adminservice.aop.PreAcquireNamespaceLock;
import lucky.apollo.common.entity.dto.ItemChangeSetsDTO;
import lucky.apollo.core.service.ItemSetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@RestController
public class ItemSetController {

    private final ItemSetService itemSetService;

    public ItemSetController(final ItemSetService itemSetService) {
        this.itemSetService = itemSetService;
    }

    @PreAcquireNamespaceLock
    @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/itemset")
    public ResponseEntity<Void> create(@PathVariable String appId, @PathVariable String clusterName,
                                       @PathVariable String namespaceName, @RequestBody ItemChangeSetsDTO changeSet) {

        itemSetService.updateSet(appId, clusterName, namespaceName, changeSet);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


}