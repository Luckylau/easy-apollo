package lucky.apollo.portal.listener;

import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.entity.dto.AppDTO;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.portal.api.AdminServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Component
@Slf4j
public class CreationListener {

    @Autowired
    private AdminServiceApi adminServiceApi;

    @EventListener
    public void onAppCreationEvent(AppCreationEvent event) {
        AppDTO appDTO = BeanUtils.transformWithIgnoreNull(AppDTO.class, event.getApp());
        try {
            log.info("Create app, appId = {}", appDTO.getAppId());
            adminServiceApi.createApp(appDTO);
        } catch (Throwable e) {
            log.error("Create app failed. appId = {}", appDTO.getAppId(), e);
        }
    }

}
