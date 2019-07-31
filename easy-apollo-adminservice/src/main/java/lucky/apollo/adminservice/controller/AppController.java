package lucky.apollo.adminservice.controller;


import lucky.apollo.common.entity.dto.AppDTO;
import lucky.apollo.common.entity.po.AppPO;
import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.common.exception.NotFoundException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.service.AdminService;
import lucky.apollo.core.service.AppService;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@RestController
public class AppController {

    private final AppService appService;
    private final AdminService adminService;

    public AppController(final AppService appService, final AdminService adminService) {
        this.appService = appService;
        this.adminService = adminService;
    }

    @PostMapping("/apps")
    public AppDTO create(@Valid @RequestBody AppDTO dto) {
        AppPO entity = BeanUtils.transformWithIgnoreNull(AppPO.class, dto);
        AppPO managedEntity = appService.findOne(entity.getAppId());
        if (managedEntity != null) {
            throw new BadRequestException("app already exist.");
        }

        entity = adminService.createNewApp(entity);

        return BeanUtils.transformWithIgnoreNull(AppDTO.class, entity);
    }

    @DeleteMapping("/apps/{appId:.+}")
    public void delete(@PathVariable("appId") String appId, @RequestParam String operator) {
        AppPO entity = appService.findOne(appId);
        if (entity == null) {
            throw new NotFoundException("app not found for appId " + appId);
        }
        adminService.deleteApp(entity, operator);
    }

    @PutMapping("/apps/{appId:.+}")
    public void update(@PathVariable String appId, @RequestBody AppPO app) {
        if (!Objects.equals(appId, app.getAppId())) {
            throw new BadRequestException("The App Id of path variable and request body is different");
        }

        appService.update(app);
    }

    @GetMapping("/apps")
    public List<AppDTO> find(@RequestParam(value = "name", required = false) String name,
                             Pageable pageable) {
        List<AppPO> app = null;
        if (StringUtils.isBlank(name)) {
            app = appService.findAll(pageable);
        } else {
            app = appService.findByName(name);
        }
        return BeanUtils.batchTransformWithIgnoreNull(AppDTO.class, app);
    }

    @GetMapping("/apps/{appId:.+}")
    public AppDTO get(@PathVariable("appId") String appId) {
        AppPO app = appService.findOne(appId);
        if (app == null) {
            throw new NotFoundException("app not found for appId " + appId);
        }
        return BeanUtils.transformWithIgnoreNull(AppDTO.class, app);
    }

    @GetMapping("/apps/{appId}/unique")
    public boolean isAppIdUnique(@PathVariable("appId") String appId) {
        return appService.isAppIdUnique(appId);
    }
}