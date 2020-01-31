package lucky.apollo.configservice;

import lucky.apollo.common.ApolloCommonConfig;
import lucky.apollo.core.ApolloCoreConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author luckylau
 * @Date 2019/12/4
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableEurekaClient
@ComponentScan(basePackageClasses = {
        ConfigServiceApplication.class,
        ApolloCoreConfig.class,
        ApolloCommonConfig.class})
public class ConfigServiceApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ConfigServiceApplication.class, args);
    }

}