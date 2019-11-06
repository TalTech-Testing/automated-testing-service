package ee.taltech.arete.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Custom properties for Spring Boot configuration.
 */
@ConfigurationProperties(prefix = "testing")
@Configuration
@Data
public class PropertyConfig {

}
