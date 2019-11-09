package ee.taltech.arete.initializers;

import ee.taltech.arete.domain.Submission;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Random;

public class SubmissionInitializer {
	private final static String UNIID_DOCKER = "uniid";
	private final static String UNIID_GIT = "envomp";
	private final static String[] ACCEPTED_TEST_SLUGS = new String[]{"EX01IdCode", "EX02Cpu", "EX03SocialNetwork"};
	private final static String[] SLUGS = new String[]{"EX01IdCode"}; //, "EX02Cpu", "EX03SocialNetwork"
	private final static String TESTING_PLATFORM = "java";
	private static final String PROJECT_DOCKER = "iti69-420";
	private static final String PROJECT_GIT = "iti0202-2019";
	private static final String PROJECT_BASE = "ex";
	private final static String RETURN_URL = "neti.ee";
	private final static String[] EXTRA = new String[]{"style"};

	public static Submission getFullSubmission() {
		return Submission.builder()
				.uniid(UNIID_GIT)
				.hash(getRandomHash())
				.testingPlatform(TESTING_PLATFORM)
				.returnUrl(RETURN_URL)
				.extra(EXTRA)
				.slugs(new String[]{ACCEPTED_TEST_SLUGS[new Random().nextInt(3)]})
				.project(PROJECT_GIT)
				.projectBase(PROJECT_BASE)
				.timestamp(System.currentTimeMillis())
				.priority(new Random().nextInt(10) + 1)
				.build();
	}

	public static Submission getControllerEndpointSubmission() {
		return Submission.builder()
				.uniid(UNIID_GIT)
				.hash(getRandomHash())
				.testingPlatform(TESTING_PLATFORM)
				.returnUrl(RETURN_URL)
				.extra(EXTRA)
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
		jsonObject.put("hash", getRandomHash());
		jsonObject.put("testingPlatform", TESTING_PLATFORM);
		jsonObject.put("returnUrl", RETURN_URL);
		jsonObject.put("extra", jsonArray);

		return jsonObject.toString();
	}

	private static String getRandomHash() {
		return RandomStringUtils.random(64, true, true).toLowerCase();
	}


	public static void assertFullSubmission(Submission submission) {
		assert submission.getUniid().equals(UNIID_GIT);
		assert submission.getHash().length() == 64;
		assert submission.getReturnUrl().equals(RETURN_URL);
		assert submission.getTestingPlatform().equals(TESTING_PLATFORM);
		assert Arrays.equals(submission.getExtra(), EXTRA);
	}

}
