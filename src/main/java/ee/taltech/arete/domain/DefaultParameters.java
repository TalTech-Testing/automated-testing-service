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

	private Set<String> groupingFolders;

	private String solutionsRepository;

	private String programmingLanguage;

	public void overrideParametersForStudent(Submission submission) {
		if (systemExtra != null) {
			if (systemExtra.contains("noMail")) {
				submission.getSystemExtra().add("noMail");
			}
		}
	}

	public void overrideParametersForTestValidation(Submission submission) {
		overrideDefaultParameters(submission);

		if (solutionsRepository != null) {
			submission.setGitStudentRepo(solutionsRepository);
		}
	}

	public void overrideParametersForStudentValidation(Submission submission) {
		overrideDefaultParameters(submission);
	}

	private void overrideDefaultParameters(Submission submission) {
		if (dockerTimeout != null) {
			submission.setDockerTimeout(dockerTimeout);
		}

		if (dockerExtra != null) {
			submission.setDockerExtra(dockerExtra);
		}

		if (systemExtra != null) {
			submission.setSystemExtra(systemExtra);
		}

		if (groupingFolders != null) {
			submission.setGroupingFolders(groupingFolders);
		}

		if (programmingLanguage != null) {
			submission.setTestingPlatform(programmingLanguage);
		}
	}
}
