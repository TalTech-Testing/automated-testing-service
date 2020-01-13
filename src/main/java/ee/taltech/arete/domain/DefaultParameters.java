package ee.taltech.arete.domain;

import lombok.*;

import java.util.Set;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DefaultParameters {

	private Integer dockerTimeout;

	private Set<String> dockerExtra;

	private Set<String> systemExtra;

	public void overrideDefaults(Submission submission) {
		if (dockerTimeout != null) {
			submission.setDockerTimeout(dockerTimeout);
		}

		if (dockerExtra != null) {
			submission.setDockerExtra(dockerExtra);
		}

		if (systemExtra != null) {
			submission.setSystemExtra(systemExtra);
		}
	}

}