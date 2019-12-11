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

public class SubmissionInitializer {
	private final static String UNIID_GIT = "envomp";
	private static final String STUDENT_REPO_PYTHON = "https://gitlab.cs.ttu.ee/envomp/iti0102-2019";
	private static final String STUDENT_REPO = "https://gitlab.cs.ttu.ee/envomp/iti0202-2019";
	private static final String PROJECT_PYTHON = "iti0102-2019";
	private static final String PROJECT = "iti0202-2019";
	private final static String TESTING_PLATFORM = "java";
	private final static String TESTING_PLATFORM_PYTHON = "python";
	private static final String PROJECT_GIT = "https://gitlab.cs.ttu.ee/iti0202-2019/ex";
	private static final String PROJECT_GIT_PYTHON = "https://gitlab.cs.ttu.ee/iti0102-2019/ex";
	private final static String RETURN_URL = "https://jsonplaceholder.typicode.com/posts";
	private final static String[] EXTRA = new String[]{"stylecheck"};

	public static Submission getFullSubmissionPython() {

		return Submission.builder()
				.uniid(UNIID_GIT)
				.gitStudentRepo(STUDENT_REPO_PYTHON)
				.project(PROJECT_PYTHON)
				.hash("fb23ca3217bc9051241b56488a100e6d744201ef")
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.returnUrl(RETURN_URL)
				.dockerTimeout(120)
//				.systemExtra(new String[]{"noMail"})
				.systemExtra(new String[]{})
				.dockerExtra(new String[]{"stylecheck"})
				.timestamp(System.currentTimeMillis())
				.gitTestSource(PROJECT_GIT_PYTHON)
				.priority(10)
				.build();
	}


	public static Submission getFullSubmissionJava() {

		return Submission.builder()
				.uniid(UNIID_GIT)
				.gitStudentRepo(STUDENT_REPO)
				.project(PROJECT)
				.hash("8133c4fb0dbcda3709d9f8ced953f5ef5af4e0ca")
				.testingPlatform(TESTING_PLATFORM)
				.returnUrl(RETURN_URL)
				.dockerTimeout(120)
//				.systemExtra(new String[]{"noMail"})
				.systemExtra(new String[]{})
				.dockerExtra(new String[]{"stylecheck"})
				.gitTestSource(PROJECT_GIT)
				.timestamp(System.currentTimeMillis())
				.priority(10)
				.build();
	}


	public static Submission getControllerEndpointSubmission() {
		return Submission.builder()
				.uniid(UNIID_GIT)
				.gitStudentRepo(STUDENT_REPO)
				.project(PROJECT)
				.testingPlatform(TESTING_PLATFORM)
				.returnUrl(RETURN_URL)
				.systemExtra(new String[]{"noMail"})
				.dockerExtra(new String[]{"stylecheck"})
				.hash("d3f5510928bb8dacc20d29110e9268756418bef9")
				.gitTestSource(PROJECT_GIT)
				.dockerExtra(EXTRA)
				.build();
	}

	public static AreteRequestAsync getFullSubmissionString() {

		return AreteRequestAsync.builder()
				.gitStudentRepo(STUDENT_REPO)
				.hash("2448474b6a76ef534660817948dc8b816e40dd48")
				.testingPlatform(TESTING_PLATFORM)
				.returnUrl(RETURN_URL)
				.dockerExtra(EXTRA)
				.gitTestSource(PROJECT_GIT)
				.build();
	}


	public static AreteRequestSync getFullSubmissionStringSync(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequestSync.builder()
				.testingPlatform(TESTING_PLATFORM)
				.gitTestSource(PROJECT_GIT)
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequestSync.SourceFile.builder()
								.path("EX01IdCode\\src\\ee\\taltech\\iti0202\\idcode\\IDCode.java")
								.contents(Files.readString(Paths.get("src\\test\\java\\ee\\taltech\\arete\\initializers\\IDCode.java").toAbsolutePath(), StandardCharsets.US_ASCII))
								.build())))
				.build();
	}

	public static AreteRequestSync getFullSubmissionStringPythonSync(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequestSync.builder()
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.gitTestSource(PROJECT_GIT_PYTHON)
				.dockerExtra(EXTRA)
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequestSync.SourceFile.builder()
								.path("ex04_cipher\\cipher.py")
								.contents(Files.readString(Paths.get("src\\test\\java\\ee\\taltech\\arete\\initializers\\cipher.py").toAbsolutePath(), StandardCharsets.UTF_8))
								.build())))
				.build();
	}

	public static AreteRequestSync getFullSubmissionStringPythonSyncNoStyle(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequestSync.builder()
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.gitTestSource(PROJECT_GIT_PYTHON)
				.dockerExtra(new String[]{})
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequestSync.SourceFile.builder()
								.path("ex04_cipher\\cipher.py")
								.contents(Files.readString(Paths.get("src\\test\\java\\ee\\taltech\\arete\\initializers\\cipher.py").toAbsolutePath(), StandardCharsets.UTF_8))
								.build())))
				.build();
	}


	public static AreteRequestSync getFullSubmissionStringPythonSyncNoStdout(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequestSync.builder()
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.dockerExtra(EXTRA)
				.systemExtra(new String[]{"noStd"})
				.gitTestSource(PROJECT_GIT_PYTHON)
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequestSync.SourceFile.builder()
								.path("ex04_cipher\\cipher.py")
								.contents(Files.readString(Paths.get("src\\test\\java\\ee\\taltech\\arete\\initializers\\cipher.py").toAbsolutePath(), StandardCharsets.UTF_8))
								.build())))
				.build();
	}

	public static AreteRequestSync getFullSubmissionStringPythonSyncNoTesterFiles(String base) throws IOException {
		String hash = getRandomHash();
		return AreteRequestSync.builder()
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.dockerExtra(EXTRA)
				.systemExtra(new String[]{"noTesterFiles"})
				.gitTestSource(PROJECT_GIT_PYTHON)
				.hash(hash)
				.returnUrl(String.format("%s/waitingroom/%s", base, hash))
				.source(new ArrayList<>(Collections.singletonList(
						AreteRequestSync.SourceFile.builder()
								.path("ex04_cipher\\cipher.py")
								.contents(Files.readString(Paths.get("src\\test\\java\\ee\\taltech\\arete\\initializers\\cipher.py").toAbsolutePath(), StandardCharsets.UTF_8))
								.build())))
				.build();
	}

	private static String getRandomHash() {
		return RandomStringUtils.random(64, true, true).toLowerCase();
	}


	public static void assertFullSubmission(Submission submission) {
		assert submission.getUniid().equals(UNIID_GIT);
//		assert submission.getHash().length() == 40;
		assert submission.getReturnUrl().equals(RETURN_URL);
//		assert submission.getTestingPlatform().equals(TESTING_PLATFORM);
		assert Arrays.equals(submission.getDockerExtra(), EXTRA);
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

