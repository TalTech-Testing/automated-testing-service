package ee.taltech.arete.initializers;

import ee.taltech.arete.api.data.request.AreteRequestAsync;
import ee.taltech.arete.api.data.request.AreteRequestSync;
import ee.taltech.arete.domain.Submission;
import org.apache.commons.lang.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class SubmissionInitializer {
	private final static String UNIID_GIT = "envomp";
	private static final String STUDENT_REPO_PYTHON = System.getenv().containsKey("GITLAB_PASSWORD") ? "https://gitlab.cs.ttu.ee/envomp/iti0102-2019.git" : "git@gitlab.cs.ttu.ee:envomp/iti0102-2019.git";
	private static final String STUDENT_REPO = System.getenv().containsKey("GITLAB_PASSWORD") ? "https://gitlab.cs.ttu.ee/envomp/iti0202-2019.git" : "git@gitlab.cs.ttu.ee:envomp/iti0202-2019.git";
	private static final String STUDENT_REPO_EXAM = System.getenv().containsKey("GITLAB_PASSWORD") ? "https://gitlab.cs.ttu.ee/iti0102-2018/exams/exam2-envomp.git" : "git@gitlab.cs.ttu.ee:iti0102-2018/exams/exam2-envomp.git";
	private static final String PROJECT_PYTHON = "iti0102-2019";
	private static final String PROJECT = "iti0202-2019";
	private final static String TESTING_PLATFORM = "java";
	private final static String TESTING_PLATFORM_PYTHON = "python";
	private static final String PROJECT_GIT = System.getenv().containsKey("GITLAB_PASSWORD") ? "https://gitlab.cs.ttu.ee/iti0202-2019/ex.git" : "git@gitlab.cs.ttu.ee:iti0202-2019/ex.git";
	private static final String PROJECT_GIT_PYTHON = System.getenv().containsKey("GITLAB_PASSWORD") ? "https://gitlab.cs.ttu.ee/iti0102-2019/ex.git" : "git@gitlab.cs.ttu.ee:iti0102-2019/ex.git";
	private final static String RETURN_URL = "https://jsonplaceholder.typicode.com/posts";
	private final static HashSet<String> EXTRA = new HashSet<>(Collections.singletonList("stylecheck"));
	private final static String home = System.getenv().getOrDefault("ARETE_HOME", System.getenv("HOME") + "/arete");

	public static Submission getFullSubmissionPython() {

		return Submission.builder()
				.uniid(UNIID_GIT)
				.gitStudentRepo(STUDENT_REPO_PYTHON)
				.course(PROJECT_PYTHON)
				.folder(PROJECT_PYTHON)
				.token("Token")
				.hash("1bf2d711ce9ff944c7c9ffd9def23d312e9c4f9f")
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.returnUrl(RETURN_URL)
				.dockerTimeout(120)
//				.systemExtra(new HashSet<>())
				.systemExtra((new HashSet<>(Arrays.asList(
						"noMail"
				))))
				.dockerExtra(new HashSet<>(Collections.singletonList("stylecheck")))
				.timestamp(System.currentTimeMillis())
				.priority(10)
				.build();
	}


	public static Submission getFullSubmissionJava() {

		return Submission.builder()
				.uniid(UNIID_GIT)
				.gitStudentRepo(STUDENT_REPO)
				.course(PROJECT)
				.folder(PROJECT)
				.token("Token")
				.hash("8133c4fb0dbcda3709d9f8ced953f5ef5af4e0ca")
				.testingPlatform(TESTING_PLATFORM)
				.returnUrl(RETURN_URL)
				.dockerTimeout(120)
//				.systemExtra(new HashSet<>())
				.systemExtra((new HashSet<>(Arrays.asList(
						"noMail"
				))))
				.dockerExtra(new HashSet<>(Collections.singletonList("stylecheck")))
				.timestamp(System.currentTimeMillis())
				.priority(10)
				.build();
	}


	public static Submission getGitPullEndpointSubmission() {
		return Submission.builder()
				.uniid(UNIID_GIT)
				.gitStudentRepo(STUDENT_REPO_PYTHON)
				.course(PROJECT_PYTHON)
				.folder(PROJECT_PYTHON)
				.token("Token")
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.returnUrl(RETURN_URL)
				.systemExtra((new HashSet<>(Arrays.asList(
////						, "noMail"
				))))
				.dockerExtra(new HashSet<>(Collections.singletonList("stylecheck")))
				.hash("d3f5510928bb8dacc20d29110e9268756418bef9")
				.build();
	}

	public static AreteRequestAsync getFullSubmissionStringControllerEndpoint() {

		return AreteRequestAsync.builder()
				.gitStudentRepo(STUDENT_REPO)
				.hash("2448474b6a76ef534660817948dc8b816e40dd48")
				.testingPlatform(TESTING_PLATFORM)
				.systemExtra((new HashSet<>(Arrays.asList(
						"noMail"
				))))
				.returnUrl(RETURN_URL)
				.dockerExtra(EXTRA)
				.build();
	}

	public static AreteRequestAsync getFullSubmissionStringControllerEndpointPython() {

		return AreteRequestAsync.builder()
				.gitStudentRepo(STUDENT_REPO_PYTHON)
				.hash("1bf2d711ce9ff944c7c9ffd9def23d312e9c4f9f")
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.systemExtra((new HashSet<>(Arrays.asList(
						"noMail"
				))))
				.returnUrl(RETURN_URL)
				.dockerExtra(EXTRA)
				.build();
	}

	public static AreteRequestAsync getFullSubmissionStringControllerEndpointPythonLong() {

		return AreteRequestAsync.builder()
				.gitStudentRepo(STUDENT_REPO_PYTHON)
				.hash("a932ed61340fbaa08e308f591d5b5791044abc0c")
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.systemExtra((new HashSet<>(Arrays.asList(
////						, "noMail"
				))))
				.returnUrl(RETURN_URL)
				.dockerExtra(EXTRA)
				.dockerTimeout(10)
				.build();
	}

	public static AreteRequestAsync getFullSubmissionStringControllerEndpointPythonRecursion() {

		return AreteRequestAsync.builder()
				.gitStudentRepo("https://gitlab.cs.ttu.ee/kreban/iti0102-2019.git")
				.hash("7c39a45ab725f6106fc1b5fe06ef531ae3265825")
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.systemExtra((new HashSet<>(Arrays.asList(
//						, "noMail"
				))))
				.returnUrl(RETURN_URL)
				.dockerExtra(EXTRA)
				.priority(10)
				.build();
	}

	public static AreteRequestAsync getFullSubmissionStringExamControllerEndpoint() {

		return AreteRequestAsync.builder()
				.gitStudentRepo(STUDENT_REPO_EXAM)
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.systemExtra((new HashSet<>(Arrays.asList("noStd", "noFeedback", "noMail"))))
				.uniid("envomp")
				.returnUrl(RETURN_URL)
				.dockerExtra(EXTRA)
				.build();
	}

	public static AreteRequestSync getFullSubmissionStringSync(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequestSync.builder()
				.testingPlatform(TESTING_PLATFORM)
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.gitTestSource(PROJECT_GIT)
				.systemExtra((new HashSet<>(Arrays.asList(
////						, "noMail"
				))))
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequestSync.SourceFile.builder()
								.path("EX01IdCode/src/ee/taltech/iti0202/idcode/IDCode.java")
								.contents(Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/initializers/IDCode.java"), StandardCharsets.US_ASCII))
								.build())))
				.build();
	}


	public static AreteRequestSync getFullSubmissionStringPythonSync(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequestSync.builder()
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.dockerExtra(EXTRA)
				.hash(hash)
				.systemExtra((new HashSet<>(Arrays.asList(
////						, "noMail"
				))))
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.gitTestSource(PROJECT_GIT_PYTHON)
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequestSync.SourceFile.builder()
								.path("ex04_cipher/cipher.py")
								.contents(Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/initializers/cipher.py"), StandardCharsets.UTF_8))
								.build())))
				.build();
	}

	public static AreteRequestSync getFullSubmissionStringPythonSyncNoStyle(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequestSync.builder()
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.dockerExtra(new HashSet<>())
				.hash(hash)
				.systemExtra((new HashSet<>(Arrays.asList(
////						, "noMail"
				))))
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.gitTestSource(PROJECT_GIT_PYTHON)
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequestSync.SourceFile.builder()
								.path("ex04_cipher/cipher.py")
								.contents(Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/initializers/cipher.py"), StandardCharsets.UTF_8))
								.build())))
				.build();
	}


	public static AreteRequestSync getFullSubmissionStringPythonSyncNoStdout(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequestSync.builder()
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.dockerExtra(EXTRA)
				.systemExtra(new HashSet<>(Arrays.asList("noStd"
//						, "noMail"
				)))
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.gitTestSource(PROJECT_GIT_PYTHON)
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequestSync.SourceFile.builder()
								.path("ex04_cipher/cipher.py")
								.contents(Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/initializers/cipher.py"), StandardCharsets.UTF_8))
								.build())))
				.build();
	}

	public static AreteRequestSync getFullSubmissionStringPythonSyncNoTesterFiles(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequestSync.builder()
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.dockerExtra(EXTRA)
				.systemExtra(new HashSet<>(Arrays.asList("noTesterFiles"
//						, "noMail"
				)))
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.gitTestSource(PROJECT_GIT_PYTHON)
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequestSync.SourceFile.builder()
								.path("ex04_cipher/cipher.py")
								.contents(Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/initializers/cipher.py"), StandardCharsets.UTF_8))
								.build())))
				.build();
	}

	private static String getRandomHash() {
		return RandomStringUtils.random(64, true, true).toLowerCase();
	}


	public static void assertFullSubmission(Submission submission) {
		assert submission.getUniid() != null;
//		assert submission.getHash() != null;
		assert submission.getReturnUrl().equals(RETURN_URL);
//		assert submission.getTestingPlatform().equals(TESTING_PLATFORM);
		assert !submission.getDockerExtra().isEmpty();
	}

	public static void endTest() {

		try {
			File f = new File("students/");
			deleteDirectory(f);
		} catch (Exception ignored) {
		}

		try {
			File f = new File("tests/");
			deleteDirectory(f);
		} catch (Exception ignored) {
		}
	}

	private static void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
	}
}
