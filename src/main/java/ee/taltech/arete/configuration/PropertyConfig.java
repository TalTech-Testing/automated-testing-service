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
    /**
     * Name of the folder which docker containers will use to exchange files. The location of the shared
     * folder in the host machine will be mapped to this name.
     */
    private String sharedFolder;

    /**
     * Common prefix shared by tester docker images, e.g "hodor_".
     */
    private String testerPrefix;

    /**
     * Timeout value in seconds. The system will wait this long for the tester docker container
     * to finish running. If it fails to exit, the thread will be interrupted and container removed forcibly.
     */
    private int dockerTimeout;

    /**
     * Formatting pattern for tester log file locations.
     */
    private String dockerLogPattern;

    /**
     * Formatting pattern for docker shared folder mapping.
     */
    private String dockerMappingPattern;

    /**
     * Default prefix to use when creating temporary directories.
     */
    private String folderPrefix;
}
