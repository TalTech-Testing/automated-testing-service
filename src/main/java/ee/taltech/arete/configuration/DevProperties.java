package ee.taltech.arete.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("arete.dev")
@Component
@Data
public class DevProperties {

    private Boolean debug = true; // if unlock features for debug
    private String developer = "ago.luberg"; // send all submissions
    private String ago = "ago.luberg"; // send only failed submissions
    private String areteMail = "automated_testing_service@taltech.ee";
    private Integer defaultDockerTimeout = 120; // default dockertimeout is 120 seconds
    private String areteBackend = "http://localhost:8001/admin/job";

}
