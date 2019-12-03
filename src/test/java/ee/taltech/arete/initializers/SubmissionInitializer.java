package ee.taltech.arete.initializers;

import ee.taltech.arete.domain.Submission;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class SubmissionInitializer {
	private final static String UNIID_DOCKER = "uniid";
	private final static String UNIID_GIT = "envomp";
	private final static String TESTING_PLATFORM = "java";
	private final static String TESTING_PLATFORM_PYTHON = "python";
	private static final String PROJECT_DOCKER = "iti69-420";
	private static final String PROJECT_GIT = "iti0202-2019";
	private static final String PROJECT_GIT_PYTHON = "iti0102-2019";
	private static final String PROJECT_BASE = "ex";
	private final static String RETURN_URL = "https://jsonplaceholder.typicode.com/posts";
	private final static String[] EXTRA = new String[]{"stylecheck"};

	public static Submission getFullSubmission() {

		return Submission.builder()
				.uniid(UNIID_GIT)
				.hash("fb23ca3217bc9051241b56488a100e6d744201ef")
				.testingPlatform(TESTING_PLATFORM_PYTHON)
				.returnUrl(RETURN_URL)
				.dockerTimeout(120)
				.systemExtra(new String[]{"noMail"})
				.dockerExtra(new String[]{"stylecheck"})
				.project(PROJECT_GIT_PYTHON)
				.projectBase(PROJECT_BASE)
//				.timestamp(System.currentTimeMillis())
				.priority(new Random().nextInt(5) + 5)
				.build();
	}

	public static Submission getControllerEndpointSubmission() {
		return Submission.builder()
				.uniid(UNIID_GIT)
				.testingPlatform(TESTING_PLATFORM)
				.returnUrl(RETURN_URL)
				.hash("d3f5510928bb8dacc20d29110e9268756418bef9")
				.dockerExtra(EXTRA)
				.projectBase(PROJECT_BASE)
				.project(PROJECT_GIT)
				.build();
	}

	public static String getFullSubmissionString() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (String extra : EXTRA) {
			jsonArray.put(extra);
		}
		jsonObject.put("uniid", UNIID_GIT);
		jsonObject.put("hash", "2448474b6a76ef534660817948dc8b816e40dd48");
		jsonObject.put("testingPlatform", TESTING_PLATFORM);
		jsonObject.put("returnUrl", RETURN_URL);
		jsonObject.put("dockerExtra", jsonArray);
		jsonObject.put("project", PROJECT_GIT);

		System.out.println(jsonObject.toString());

		return jsonObject.toString();
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

