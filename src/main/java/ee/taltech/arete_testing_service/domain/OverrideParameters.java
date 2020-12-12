package ee.taltech.arete_testing_service.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OverrideParameters {

	private String dockerContentRoot;
	private String dockerContentRootBefore;

	private String dockerExtra;
	private String dockerExtraBefore;

	private String dockerTestRoot;
	private String dockerTestRootBefore;

	private Integer dockerTimeout;
	private Integer dockerTimeoutBefore;

	private Set<String> groupingFolders;
	private Set<String> groupingFoldersBefore;

	private String solutionsRepository;
	private String solutionsRepositoryBefore;

	private Set<String> systemExtra;
	private Set<String> systemExtraBefore;

	private String testingPlatform;
	private String testingPlatformBefore;

	public void overrideParametersForStudent(Submission submission) {
		if (systemExtra != null) {
			if (systemExtra.contains("noMail")) {
				submission.getSystemExtra().add("noMail");
			}
		}

		if (submission.getSystemExtra().contains("overrideContentRoot")) {
			if (dockerContentRoot != null) {
				submission.setDockerContentRoot(dockerContentRoot);
			}
		}

		if (submission.getSystemExtra().contains("overrideTestRoot")) {
			if (dockerTestRoot != null) {
				submission.setDockerTestRoot(dockerTestRoot);
			}
		}

		if (submission.getSystemExtra().contains("overrideExtra")) {
			if (dockerExtra != null) {
				submission.setDockerExtra(dockerExtra);
			}
		}

		if (submission.getSystemExtra().contains("overrideTestingPlatform")) {
			if (testingPlatform != null) {
				submission.setTestingPlatform(testingPlatform);
			}
		}

	}

	public void overrideParametersForTestValidation(Submission submission) {
		overrideParameters(submission);

		if (solutionsRepository != null) {
			submission.setGitStudentRepo(solutionsRepository);
		}
	}

	public void overrideParameters(Submission submission) {
		if (dockerTimeout != null) {
			submission.setDockerTimeout(dockerTimeout);
		}

		if (dockerContentRoot != null) {
			submission.setDockerContentRoot(dockerContentRoot);
		}

		if (dockerTestRoot != null) {
			submission.setDockerTestRoot(dockerTestRoot);
		}

		if (dockerExtra != null) {
			submission.setDockerExtra(dockerExtra);
		}

		if (systemExtra != null) {
			if (submission.getSystemExtra().contains("allowAppending") || systemExtra.contains("allowAppending")) {
				submission.getSystemExtra().addAll(systemExtra);
			} else {
				submission.setSystemExtra(systemExtra);
			}
		}

		if (groupingFolders != null) {
			if (submission.getSystemExtra().contains("allowAppending") || systemExtra.contains("allowAppending")) {
				submission.getGroupingFolders().addAll(groupingFolders);
			} else {
				submission.setGroupingFolders(groupingFolders);
			}
		}

		if (testingPlatform != null) {
			submission.setTestingPlatform(testingPlatform);
		}
	}

	public void invoke(Submission submission) {
		dockerContentRootBefore = submission.getDockerContentRoot();
		dockerExtraBefore = submission.getDockerExtra();
		dockerTestRootBefore = submission.getDockerTestRoot();
		dockerTimeoutBefore = submission.getDockerTimeout();
		groupingFoldersBefore = new HashSet<>();
		groupingFoldersBefore.addAll(submission.getGroupingFolders());
		solutionsRepositoryBefore = submission.getGitStudentRepo();
		systemExtraBefore = new HashSet<>();
		systemExtraBefore.addAll(submission.getSystemExtra());
		testingPlatformBefore = submission.getTestingPlatform();
	}

	public void revert(Submission submission) {
		submission.setDockerContentRoot(dockerContentRootBefore);
		submission.setDockerExtra(dockerExtraBefore);
		submission.setDockerTestRoot(dockerTestRootBefore);
		submission.setDockerTimeout(dockerTimeoutBefore);
		submission.setGroupingFolders(groupingFoldersBefore);
		submission.setGitStudentRepo(solutionsRepositoryBefore);
		submission.setSystemExtra(systemExtraBefore);
		submission.setTestingPlatform(testingPlatformBefore);
	}
}
