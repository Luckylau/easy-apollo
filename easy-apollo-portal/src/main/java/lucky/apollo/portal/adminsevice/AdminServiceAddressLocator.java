package lucky.apollo.portal.adminsevice;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.constant.Env;
import lucky.apollo.common.entity.dto.ServiceDTO;
import lucky.apollo.common.utils.ApolloThreadFactory;
import lucky.apollo.portal.component.rest.RestTemplateFactory;
import lucky.apollo.portal.config.PortalConfig;
import lucky.apollo.portal.metaservice.MetaDomainConsts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author luckylau
 * @Date 2019/9/17
 */
@Component
@Slf4j
public class AdminServiceAddressLocator {

    private static final String ADMIN_SERVICE_URL_PATH = "/services/admin";
    private static final long NORMAL_REFRESH_INTERVAL = 5 * 60 * 1000;
    private static final long OFFLINE_REFRESH_INTERVAL = 10 * 1000;
    private static final int RETRY_TIMES = 3;

    @Autowired
    private RestTemplateFactory restTemplateFactory;

    @Autowired
    private PortalConfig portalConfig;

    private Env env;

    private RestTemplate restTemplate;

    private ScheduledExecutorService refreshServiceAddressService;

    private List<ServiceDTO> cache = new ArrayList<>();

    @PostConstruct
    private void init() {
        restTemplate = restTemplateFactory.getObject();
        refreshServiceAddressService = new ScheduledThreadPoolExecutor(1, ApolloThreadFactory.create("AdminServiceAddressLocator", true));
        refreshServiceAddressService.schedule(new RefreshAdminServerAddressTask(), 1, TimeUnit.SECONDS);
        env = portalConfig.getActiveEnv();
    }

    private class RefreshAdminServerAddressTask implements Runnable {

        @Override
        public void run() {
            boolean refreshSucess = true;
            refreshSucess = refreshSucess && refreshServerAddressCache();
            if (refreshSucess) {
                refreshServiceAddressService
                        .schedule(new RefreshAdminServerAddressTask(), NORMAL_REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
            } else {
                refreshServiceAddressService
                        .schedule(new RefreshAdminServerAddressTask(), OFFLINE_REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
            }

        }
    }

    private boolean refreshServerAddressCache() {
        for (int i = 0; i < RETRY_TIMES; i++) {
            try {
                ServiceDTO[] serviceDTOS = getAdminServerAddress();
                if (serviceDTOS == null || serviceDTOS.length == 0) {
                    continue;
                }
                cache.addAll(Lists.newArrayList(serviceDTOS));
                return true;
            } catch (Exception e) {
                log.error(String.format("Get admin server address from meta server failed. env: %s, meta server address:%s",
                        env, MetaDomainConsts.getDomain()), e);
            }
        }

        return false;
    }

    private ServiceDTO[] getAdminServerAddress() {
        String domainName = MetaDomainConsts.getDomain();
        String url = domainName + ADMIN_SERVICE_URL_PATH;
        return restTemplate.getForObject(url, ServiceDTO[].class);
    }

    public List<ServiceDTO> getServiceList() {
        if (CollectionUtils.isEmpty(cache)) {
            return Collections.emptyList();
        }
        List<ServiceDTO> randomConfigServices = Lists.newArrayList(cache);
        Collections.shuffle(randomConfigServices);
        return randomConfigServices;
    }


}
