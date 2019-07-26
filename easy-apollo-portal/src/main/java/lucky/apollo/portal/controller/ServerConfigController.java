package lucky.apollo.portal.controller;

import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.portal.entity.bo.ServerConfigInfo;
import lucky.apollo.portal.entity.po.ServerConfigPO;
import lucky.apollo.portal.repository.ServerConfigRepository;
import lucky.apollo.portal.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author luckylau
 * @Date 2019/7/15
 */
@RestController
@RequestMapping("/server/config")
public class ServerConfigController {

    private final UserService userService;

    private final ServerConfigRepository serverConfigRepository;


    public ServerConfigController(UserService userService, ServerConfigRepository serverConfigRepository) {
        this.userService = userService;
        this.serverConfigRepository = serverConfigRepository;
    }

    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @PostMapping()
    public ServerConfigPO createOrUpdate(@Valid @RequestBody ServerConfigInfo serverConfigInfo) {
        String modifiedBy = userService.getCurrentUser().getUserId();

        ServerConfigPO storedConfig = serverConfigRepository.findByKey(serverConfigInfo.getKey());

        if (storedConfig == null) {
            storedConfig = new ServerConfigPO();
            storedConfig.setDataChangeCreatedBy(modifiedBy);
        }
        storedConfig.setDataChangeLastModifiedBy(modifiedBy);
        BeanUtils.copyPropertiesWithIgnore(serverConfigInfo, storedConfig);
        return serverConfigRepository.save(storedConfig);
    }

    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @GetMapping("/{key:.+}")
    public ServerConfigPO loadServerConfig(@PathVariable String key) {
        return serverConfigRepository.findByKey(key);
    }
}