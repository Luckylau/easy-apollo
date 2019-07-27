package lucky.apollo.portal;

import lucky.apollo.common.ApolloCommonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {ApolloCommonConfig.class,
        PortalApplication.class})
public class PortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}