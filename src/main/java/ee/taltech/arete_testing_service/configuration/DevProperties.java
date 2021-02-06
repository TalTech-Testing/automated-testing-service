package ee.taltech.arete_testing_service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("arete.dev")
@Component
@Getter
@Setter
public class DevProperties {

	private String ago = "ago.luberg@ttu.ee"; // send only failed submissions
	private String areteBackend = "https://cs.ttu.ee/services/arete/api/v2/submission"; // backend url
	private String areteMail = "automated_testing_service@taltech.ee";
	private Integer defaultDockerTimeout = 120; // default dockertimeout is 120 seconds
	private String developer = "ago.luberg@ttu.ee"; // send all submissions
	private Double maxCpuUsage = 0.8; // percent that can allow more jobs
	private String nameMatcher = "^[a-zA-Z0-9\\p{L}\\-_]*$"; // regex
	private Integer parallelJobs = 16; // Total dockers running same time
	private String schoolMailMatcher = "^[a-zA-Z0-9\\p{L}-_.]+@(ttu|taltech)\\.ee$"; // regex

}
