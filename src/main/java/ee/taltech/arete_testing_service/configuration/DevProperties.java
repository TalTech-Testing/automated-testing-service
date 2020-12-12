package ee.taltech.arete_testing_service.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("arete.dev")
@Component
@Getter
public class DevProperties {

	private final String ago = "ago.luberg@ttu.ee"; // send only failed submissions

	private final String areteBackend = "https://cs.ttu.ee/services/arete/api/v2/submission"; // backend url

	private final String areteMail = "automated_testing_service@taltech.ee";

	private final Integer defaultDockerTimeout = 120; // default dockertimeout is 120 seconds

	private final String developer = "ago.luberg@ttu.ee"; // send all submissions

	private final Double maxCpuUsage = 0.8; // percent that can allow more jobs

	private final String nameMatcher = "^[a-zA-Z0-9\\p{L}-_]*$"; // regex

	private final Integer parallelJobs = 16; // Total dockers running same time

	private final String schoolMailMatcher = "^[a-zA-Z0-9\\p{L}-_.]+@(ttu|taltech)\\.ee$"; // regex

}
