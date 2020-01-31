package lucky.apollo.adminservice;

import lucky.apollo.core.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * @Author luckylau
 * @Date 2019/9/18
 */
@Component
public class AdminServiceHealthIndicator implements HealthIndicator {
    @Autowired
    private AppService appService;

    @Override
    public Health health() {
        int errorCode = check();
        if (errorCode != 0) {
            return Health.down().withDetail("Error Code", errorCode).build();
        }
        return Health.up().build();
    }

    private int check() {
        PageRequest pageable = PageRequest.of(0, 1);
        appService.findAll(pageable);
        return 0;
    }
}
