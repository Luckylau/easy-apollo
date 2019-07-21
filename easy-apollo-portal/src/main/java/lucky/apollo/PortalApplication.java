package lucky.apollo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
@SpringBootApplication(scanBasePackages = "lucky.apollo")
@EnableTransactionManagement
public class PortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}