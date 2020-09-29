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
		if (dockerTimeout != null && willOverride(submission)) {
			submission.setDockerTimeout(dockerTimeout);
		}

		if (dockerExtra != null && willOverride(submission)) {
			submission.setDockerExtra(dockerExtra);
		}

		if (systemExtra != null && willOverride(submission)) {
			submission.setSystemExtra(systemExtra);
		}

		if (groupingFolders != null && willOverride(submission)) {
			submission.setGroupingFolders(groupingFolders);
		}

		if (programmingLanguage != null && willOverride(submission)) {
			submission.setTestingPlatform(programmingLanguage);
		}
	}

	private boolean willOverride(Submission submission) {
		return !submission.getDockerExtra().contains("noOverride");
	}
}
