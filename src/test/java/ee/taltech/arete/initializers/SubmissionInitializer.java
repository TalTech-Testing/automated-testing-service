package ee.taltech.arete.initializers;

import ee.taltech.arete.domain.Submission;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Random;

public class SubmissionInitializer {
	private final static String UNIID = "envomp";
	private final static String HASH = "hash";
	private final static String TESTING_PLATFORM = "python";
	private final static String RETURN_URL = "neti.ee";
	private final static String[] EXTRA = new String[]{"style"};
//	final static long TIMESTAMP = System.currentTimeMillis();
//	final static Integer PRIORITY = new Random().nextInt(10) + 1;

	public static Submission getFullSubmission() {
		return Submission.builder()
				.uniid(UNIID)
				.hash(HASH)
				.testingPlatform(TESTING_PLATFORM)
				.returnUrl(RETURN_URL)
				.extra(EXTRA)
				.timestamp(System.currentTimeMillis())
				.priority( new Random().nextInt(10) + 1)
				.build();
	}

	public static Submission getControllerEndpointSubmission() {
		return Submission.builder()
				.uniid(UNIID)
				.hash(HASH)
				.testingPlatform(TESTING_PLATFORM)
				.returnUrl(RETURN_URL)
				.extra(EXTRA)
				.build();
	}

	public static String getFullSubmissionString() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for(String extra : EXTRA) {
			jsonArray.put(extra);
		}
		jsonObject.put("uniid", UNIID);
		jsonObject.put("hash", HASH);
		jsonObject.put("testingPlatform", TESTING_PLATFORM);
		jsonObject.put("returnUrl", RETURN_URL);
		jsonObject.put("extra", jsonArray);

		return jsonObject.toString();
	}


	public static void assertFullSubmission(Submission submission) {
		assert submission.getUniid().equals(UNIID);
		assert submission.getHash().equals(HASH);
		assert submission.getReturnUrl().equals(RETURN_URL);
		assert submission.getTestingPlatform().equals(TESTING_PLATFORM);
		assert Arrays.equals(submission.getExtra(), EXTRA);
	}

}
