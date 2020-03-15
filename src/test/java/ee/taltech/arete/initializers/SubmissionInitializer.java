package ee.taltech.arete.initializers;

import ee.taltech.arete.api.data.request.AreteRequest;
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
	private static final String STUDENT_REPO_PYTHON = "https://gitlab.cs.ttu.ee/envomp/iti0102-2019.git";
	private static final String STUDENT_REPO_JAVA = "https://gitlab.cs.ttu.ee/envomp/iti0202-2019.git";
	private static final String STUDENT_REPO_EXAM = "https://gitlab.cs.ttu.ee/iti0102-2018/exams/exam2-envomp.git";
	private static final String TESTER_REPO_PYTHON = "https://gitlab.cs.ttu.ee/iti0102-2019/ex.git";
	private static final String TESTER_REPO_EXAM = "https://gitlab.cs.ttu.ee/iti0102-2018/ex.git";
	private static final String TESTER_REPO_JAVA = "https://gitlab.cs.ttu.ee/iti0202-2019/ex.git";
	private static final String PROJECT_PYTHON = "iti0102-2019";
	private static final String PROJECT = "iti0202-2019";
	private final static String TESTING_PLATFORM_JAVA = "java";
	private final static String TESTING_PLATFORM_PYTHON = "python";
	private final static String TESTING_PLATFORM_PROLOG = "prolog";
	private static final String PROJECT_GIT = System.getenv().containsKey("GITLAB_PASSWORD") ? "https://gitlab.cs.ttu.ee/iti0202-2019/ex.git" : "git@gitlab.cs.ttu.ee:iti0202-2019/ex.git";
	private static final String PROJECT_GIT_PYTHON = System.getenv().containsKey("GITLAB_PASSWORD") ? "https://gitlab.cs.ttu.ee/iti0102-2019/ex.git" : "git@gitlab.cs.ttu.ee:iti0102-2019/ex.git";
	private final static String RETURN_URL = "https://jsonplaceholder.typicode.com/posts";
	private final static HashSet<String> EXTRA = new HashSet<>(Collections.singletonList("stylecheck"));
	private final static String home = System.getenv().getOrDefault("ARETE_HOME", System.getenv("HOME") + "/arete");

	public static Submission getFullSubmissionPython() {

		return Submission.builder()
				.uniid(UNIID_GIT)
				.gitStudentRepo(STUDENT_REPO_PYTHON)
				.gitTestSource(TESTER_REPO_PYTHON)
				.course(PROJECT_PYTHON)
				.folder(PROJECT_PYTHON)
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
				.gitStudentRepo(STUDENT_REPO_JAVA)
				.gitTestSource(TESTER_REPO_JAVA)
				.course(PROJECT)
				.folder(PROJECT)
				.hash("8133c4fb0dbcda3709d9f8ced953f5ef5af4e0ca")
				.testingPlatform(TESTING_PLATFORM_JAVA)
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
				.gitTestSource(TESTER_REPO_PYTHON)
				.course(PROJECT_PYTHON)
				.folder(PROJECT_PYTHON)
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.returnUrl(RETURN_URL)
				.systemExtra((new HashSet<>(Arrays.asList(
						"noMail"
				))))
				.dockerExtra(new HashSet<>(Collections.singletonList("stylecheck")))
				.hash("a5462dc0377504e79b25ad76c9d0a4c7ce27f7d4")
				.build();
	}

	public static AreteRequest getFullSubmissionStringControllerEndpoint() {

		return AreteRequest.builder()
				.gitStudentRepo(STUDENT_REPO_JAVA)
				.gitTestSource(TESTER_REPO_JAVA)
				.hash("2448474b6a76ef534660817948dc8b816e40dd48")
				.testingPlatform(TESTING_PLATFORM_JAVA)
				.systemExtra((new HashSet<>(Arrays.asList(
						"noMail"
				))))
				.returnUrl(RETURN_URL)
				.dockerExtra(EXTRA)
				.build();
	}

	public static AreteRequest getFullSubmissionStringControllerEndpointPython() {

		return AreteRequest.builder()
				.gitStudentRepo(STUDENT_REPO_PYTHON)
				.gitTestSource(TESTER_REPO_PYTHON)
				.hash("1bf2d711ce9ff944c7c9ffd9def23d312e9c4f9f")
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.systemExtra((new HashSet<>(Arrays.asList(
						"noMail"
				))))
				.returnUrl(RETURN_URL)
				.dockerExtra(EXTRA)
				.build();
	}

	public static AreteRequest getFullSubmissionStringControllerEndpointPythonLong() {

		return AreteRequest.builder()
				.gitStudentRepo(STUDENT_REPO_PYTHON)
				.gitTestSource(TESTER_REPO_PYTHON)
				.hash("a932ed61340fbaa08e308f591d5b5791044abc0c")
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.systemExtra((new HashSet<>(Arrays.asList(
						"noMail"
				))))
				.returnUrl(RETURN_URL)
				.dockerExtra(EXTRA)
				.dockerTimeout(1080)
				.build();
	}

	public static AreteRequest getFullSubmissionStringControllerEndpointPythonRecursion(String base) {
		String hash = getRandomHash();
		return AreteRequest.builder()
				.gitStudentRepo(STUDENT_REPO_PYTHON)
				.gitTestSource(TESTER_REPO_PYTHON)
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.systemExtra((new HashSet<>(Arrays.asList(
						"noMail"
				))))
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.hash(hash)
				.dockerExtra(EXTRA)
				.priority(10)
				.build();
	}

	public static AreteRequest getFullSubmissionStringControllerEndpointPythonCustomConfiguration() {
		return AreteRequest.builder()
				.gitStudentRepo(STUDENT_REPO_PYTHON)
				.gitTestSource(TESTER_REPO_PYTHON)
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.returnUrl(RETURN_URL)
				.priority(10)
				.build();
	}

	public static AreteRequest getFullSubmissionStringExamControllerEndpoint(String base) {
		String hash = getRandomHash();
		return AreteRequest.builder()
				.gitStudentRepo(STUDENT_REPO_EXAM)
				.gitTestSource(TESTER_REPO_EXAM)
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.systemExtra((new HashSet<>(Arrays.asList("noStd", "noMail"))))
				.uniid("envomp")
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.hash(hash)
				.dockerExtra(EXTRA)
				.build();
	}

	public static AreteRequest getFullSubmissionStringExamControllerEndpoint() {

		return AreteRequest.builder()
				.gitStudentRepo(STUDENT_REPO_EXAM)
				.gitTestSource(TESTER_REPO_EXAM)
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.systemExtra((new HashSet<>(Arrays.asList("noStd", "noFeedback", "noMail"))))
				.uniid("envomp")
				.returnUrl(RETURN_URL)
				.dockerExtra(EXTRA)
				.build();
	}

	public static AreteRequest getFullSubmissionStringProlog() {

		return AreteRequest.builder()
				.gitStudentRepo("https://gitlab.cs.ttu.ee/envomp/iti0211-2019.git")
				.testingPlatform(TESTING_PLATFORM_PROLOG)
				// no test access prolog
				.uniid("envomp")
				.returnUrl(RETURN_URL)
				.build();
	}

	public static AreteRequest getFullSubmissionStringSync(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequest.builder()
				.gitTestSource(TESTER_REPO_JAVA)
				.testingPlatform(TESTING_PLATFORM_JAVA)
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.gitTestSource(PROJECT_GIT)
				.dockerExtra((new HashSet<>(Collections.singletonList("-r ~CHECKSTYLE"))))
				.systemExtra((new HashSet<>(Collections.singletonList("noMail"))))
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequest.SourceFile.builder()
								.path("EX01IdCode/src/ee/taltech/iti0202/idcode/IDCode.java")
								.contents(Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/initializers/IDCode.java"), StandardCharsets.US_ASCII))
								.build())))
				.build();
	}

	public static AreteRequest getFullSubmissionStringSyncBadRequest(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequest.builder()
				.gitTestSource(TESTER_REPO_JAVA)
				.testingPlatform(TESTING_PLATFORM_JAVA)
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.gitTestSource(PROJECT_GIT)
				.systemExtra((new HashSet<>(Arrays.asList(
////						, "noMail"
				))))
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequest.SourceFile.builder()
								.path("EX01IdCode/src/ee/taltech/iti0202/idcode/IDCode.java")
								.contents(Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/initializers/IDCode.java"), StandardCharsets.US_ASCII))
								.build())))
				.build();
	}


	public static AreteRequest getFullSubmissionStringPythonSync(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequest.builder()
				.gitTestSource(TESTER_REPO_PYTHON)
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.dockerExtra(EXTRA)
				.hash(hash)
				.systemExtra((new HashSet<>(Arrays.asList(
////						, "noMail"
				))))
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.gitTestSource(PROJECT_GIT_PYTHON)
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequest.SourceFile.builder()
								.path("ex04_cipher/cipher.py")
								.contents(Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/initializers/cipher.py"), StandardCharsets.UTF_8))
								.build())))
				.build();
	}

	public static AreteRequest getFullSubmissionStringPythonSyncNoStyle(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequest.builder()
				.gitTestSource(TESTER_REPO_PYTHON)
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.dockerExtra(new HashSet<>())
				.hash(hash)
				.systemExtra((new HashSet<>(Arrays.asList(
////						, "noMail"
				))))
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.gitTestSource(PROJECT_GIT_PYTHON)
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequest.SourceFile.builder()
								.path("ex04_cipher/cipher.py")
								.contents(Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/initializers/cipher.py"), StandardCharsets.UTF_8))
								.build())))
				.build();
	}


	public static AreteRequest getFullSubmissionStringPythonSyncNoStdout(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequest.builder()
				.gitTestSource(TESTER_REPO_PYTHON)
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.dockerExtra(EXTRA)
				.systemExtra(new HashSet<>(Arrays.asList("noStd"
//						, "noMail"
				)))
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.gitTestSource(PROJECT_GIT_PYTHON)
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequest.SourceFile.builder()
								.path("ex04_cipher/cipher.py")
								.contents(Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/initializers/cipher.py"), StandardCharsets.UTF_8))
								.build())))
				.build();
	}

	public static AreteRequest getFullSubmissionStringPythonSyncNoTesterFiles(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequest.builder()
				.gitTestSource(TESTER_REPO_PYTHON)
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.dockerExtra(EXTRA)
				.systemExtra(new HashSet<>(Arrays.asList("noTesterFiles"
//						, "noMail"
				)))
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.gitTestSource(PROJECT_GIT_PYTHON)
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequest.SourceFile.builder()
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
//		assert !submission.getDockerExtra().isEmpty();
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
