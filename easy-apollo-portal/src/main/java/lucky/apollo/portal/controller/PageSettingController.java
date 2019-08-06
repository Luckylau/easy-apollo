package lucky.apollo.portal.controller;

import lucky.apollo.portal.config.PortalConfig;
import lucky.apollo.portal.entity.vo.PageSetting;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author luckylau
 * @Date 2019/7/27
 */
@RestController
public class PageSettingController {
    private final PortalConfig portalConfig;

    public PageSettingController(final PortalConfig portalConfig) {
        this.portalConfig = portalConfig;
    }

    @GetMapping("/page-settings")
    public PageSetting getPageSetting() {
        PageSetting setting = new PageSetting();

        setting.setWikiAddress(portalConfig.wikiAddress());

        return setting;
    }
}
