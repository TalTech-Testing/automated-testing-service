package ee.taltech.arete.controller;

import ee.taltech.arete.domain.Submission;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class SubmissionInit {
	final static String UNIID = "envomp";
	final static String HASH = "hash";
	final static String TESTING_PLATFORM = "python";
	final static String RETURN_URL = "neti.ee";
	final static String[] EXTRA = new String[]{"style"};


	public static String getFullSubmission() throws JSONException {
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
