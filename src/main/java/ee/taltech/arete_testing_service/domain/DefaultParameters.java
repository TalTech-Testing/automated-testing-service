package ee.taltech.arete_testing_service.domain;


import ee.taltech.arete.java.TestingEnvironment;
import ee.taltech.arete.java.UvaConfiguration;
import lombok.*;

import java.util.Set;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DefaultParameters {

	private TestingEnvironment testingEnvironment;

	private UvaConfiguration uvaConfiguration;

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

		if (uvaConfiguration != null && uvaConfiguration.getUserID() != null) {
			submission.getUvaConfiguration().setUserID(uvaConfiguration.getUserID());
		}
	}

	public void overrideParametersForTestValidation(Submission submission) {
		overrideDefaultParameters(submission);

		if (solutionsRepository != null) {
			submission.setGitStudentRepo(solutionsRepository);
		}
	}

	public void overrideParameters(Submission submission) {
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

		if (uvaConfiguration != null) {
			submission.setUvaConfiguration(uvaConfiguration);
		}

		if (testingEnvironment != null) {
			submission.setTestingEnvironment(testingEnvironment);
		}
	}
}
