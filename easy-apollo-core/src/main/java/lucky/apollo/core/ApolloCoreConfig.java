package lucky.apollo.core;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Hello world!
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan(basePackageClasses = ApolloCoreConfig.class)
public class ApolloCoreConfig {
}
