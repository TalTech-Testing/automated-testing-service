package ee.taltech.arete.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Custom properties for Spring Boot configuration.
 */
@Configuration
@EnableScheduling
public class PropertyConfig {

}
