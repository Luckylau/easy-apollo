package lucky.apollo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
@SpringBootApplication(scanBasePackages = "lucky.apollo")
public class PortalApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(PortalApplication.class, args);
    }
}
