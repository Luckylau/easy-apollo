package lucky.apollo.portal.controller;

import lucky.apollo.portal.config.PortalConfig;
import lucky.apollo.portal.entity.vo.Organization;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/27
 */
@RestController
@RequestMapping("/organizations")
public class OrganizationController {
    private final PortalConfig portalConfig;

    public OrganizationController(final PortalConfig portalConfig) {
        this.portalConfig = portalConfig;
    }


    @RequestMapping
    public List<Organization> loadOrganization() {
        return portalConfig.organizations();
    }

}
