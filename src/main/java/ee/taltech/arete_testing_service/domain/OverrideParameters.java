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
	private String dockerExtra;
	private String dockerTestRoot;
	private Integer dockerTimeout;
	private Integer priority;
	private Set<String> groupingFolders;
	private Set<String> testerFolders;
	private String solutionsRepository;
	private Set<String> systemExtra;
	private String testingPlatform;
	private OverrideParameters before;

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

		if (priority != null) {
			submission.setPriority(priority);
		}

		if (systemExtra != null) {
			if (submission.getSystemExtra().contains("allowAppending") ||
					(systemExtra != null && systemExtra.contains("allowAppending"))) {
				submission.getSystemExtra().addAll(systemExtra);
			} else {
				submission.setSystemExtra(systemExtra);
			}
		}

		if (groupingFolders != null) {
			if (submission.getSystemExtra().contains("allowAppending") ||
					(systemExtra != null && systemExtra.contains("allowAppending"))) {
				submission.getGroupingFolders().addAll(groupingFolders);
			} else {
				submission.setGroupingFolders(groupingFolders);
			}
		}

		if (testerFolders != null) {
			if (submission.getSystemExtra().contains("allowAppending") ||
					(systemExtra != null && systemExtra.contains("allowAppending"))) {
				submission.getTesterFolders().addAll(testerFolders);
			} else {
				submission.setTesterFolders(testerFolders);
			}
		}

		if (testingPlatform != null) {
			submission.setTestingPlatform(testingPlatform);
		}
	}

	public void invoke(Submission submission) {
		before = new OverrideParameters();
		before.setDockerContentRoot(submission.getDockerContentRoot());
		before.setDockerExtra(submission.getDockerExtra());
		before.setDockerTestRoot(submission.getDockerTestRoot());
		before.setDockerTimeout(submission.getDockerTimeout());
		before.setPriority(submission.getPriority());
		before.setGroupingFolders(new HashSet<>());
		before.getGroupingFolders().addAll(submission.getGroupingFolders());
		before.setTesterFolders(new HashSet<>());
		before.getTesterFolders().addAll(submission.getTesterFolders());
		before.setSolutionsRepository(submission.getGitStudentRepo());
		before.setSystemExtra(new HashSet<>());
		before.getSystemExtra().addAll(submission.getSystemExtra());
		before.setTestingPlatform(submission.getTestingPlatform());
	}

	public void revert(Submission submission) {
		submission.setDockerContentRoot(before.getDockerContentRoot());
		submission.setDockerExtra(before.getDockerExtra());
		submission.setDockerTestRoot(before.getDockerTestRoot());
		submission.setDockerTimeout(before.getDockerTimeout());
		submission.setPriority(before.getPriority());
		submission.setGroupingFolders(before.getGroupingFolders());
		submission.setTesterFolders(before.getTesterFolders());
		submission.setGitStudentRepo(before.getSolutionsRepository());
		submission.setSystemExtra(before.getSystemExtra());
		submission.setTestingPlatform(before.getTestingPlatform());
	}
}
