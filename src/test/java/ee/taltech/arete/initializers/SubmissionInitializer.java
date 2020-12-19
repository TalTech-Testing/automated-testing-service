package ee.taltech.arete.initializers;

import ee.taltech.arete.java.request.AreteRequestDTO;
import ee.taltech.arete.java.request.SourceFileDTO;
import ee.taltech.arete_testing_service.domain.Submission;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class SubmissionInitializer {
	private final static String UNIID_GIT = "envomp";
	private static final String STUDENT_REPO_JAVA = "https://gitlab.cs.ttu.ee/envomp/iti0202-2019.git";
	private static final String STUDENT_REPO_GITHUB = "https://github.com/envomp/CV.git";
	private static final String TESTER_REPO_JAVA = "https://gitlab.cs.ttu.ee/iti0202-2019/ex.git";
	private static final String TESTER_REPO_GITHUB = "https://github.com/envomp/CV.git";
	private static final String PROJECT = "iti0202-2019";
	private final static String TESTING_PLATFORM_JAVA = "java";

	private static String getRandomHash() {
		return RandomStringUtils.random(64, true, true).toLowerCase();
	}

	@SneakyThrows
	public static AreteRequestDTO getNormalSyncRequest() {
		return AreteRequestDTO.builder()
				.uniid(UNIID_GIT)
				.gitTestRepo(TESTER_REPO_JAVA)
				.testingPlatform(TESTING_PLATFORM_JAVA)
				.gitTestRepo(STUDENT_REPO_JAVA)
				.systemExtra((new HashSet<>(Arrays.asList("integration_tests", "noMail"))))
				.source(new ArrayList<>(Collections.singletonList(
						SourceFileDTO.builder()
								.path("EX01IdCode/src/ee/taltech/iti0202/idcode/IDCode.java")
								.contents("File content")
								.build())))
				.build();
	}

	public static Submission getGitPullEndpointSubmissionGithub(String base) {
		String hash = getRandomHash();
		return Submission.builder()
				.uniid(UNIID_GIT)
				.gitStudentRepo(STUDENT_REPO_GITHUB)
				.gitTestRepo(TESTER_REPO_GITHUB)
				.course(UNIID_GIT)
				.folder(UNIID_GIT)
				.slugs(new HashSet<>(Arrays.asList(
						"EX01IdCode/inner/stuff.py"
				)))
				.priority(10)
				.timestamp(1L)
				.dockerTimeout(120)
				.testingPlatform(TESTING_PLATFORM_JAVA)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.systemExtra((new HashSet<>(Arrays.asList("noMail", "integration_tests"))))
				.hash("a5462dc0377504e79b25ad76c9d0a4c7ce27f7d4")
				.build();
	}

	public static Submission getGitPullEndpointSubmissionGitlab(String base) {
		String hash = getRandomHash();
		return Submission.builder()
				.uniid(UNIID_GIT)
				.gitStudentRepo(STUDENT_REPO_JAVA)
				.gitTestRepo(TESTER_REPO_JAVA)
				.course(PROJECT)
				.folder(PROJECT)
				.slugs(new HashSet<>(Arrays.asList(
						"EX01IdCode/inner/stuff.py",
						"TK/tk_tsükkel_1/exam.py"
				)))
				.groupingFolders(new HashSet<>(Collections.singletonList("TK")))
				.priority(10)
				.timestamp(1L)
				.dockerTimeout(120)
				.testingPlatform(TESTING_PLATFORM_JAVA)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.systemExtra((new HashSet<>(Arrays.asList("noMail", "integration_tests"))))
				.build();
	}
}
