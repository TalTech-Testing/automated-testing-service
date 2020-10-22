package ee.taltech.arete_testing_service.service.uva;

import ee.taltech.arete.java.TestStatus;
import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete.java.response.arete.TestContextDTO;
import ee.taltech.arete.java.response.arete.UnitTestDTO;
import lombok.SneakyThrows;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UvaTestRunner {

	private static final Map<Integer, String> verdictMap = new HashMap<>() {
		{
			put(10, "Submission error");
			put(15, "Can't be judged");
			put(20, "In queue");
			put(30, "Compile error");
			put(35, "Restricted function");
			put(40, "Runtime error");
			put(45, "Output limit");
			put(50, "Time limit");
			put(60, "Memory limit");
			put(70, "Wrong answer");
			put(80, "Presentation error");
			put(90, "Accepted");
		}
	};

	private static final Map<Integer, TestStatus> verdictMapToResult = new HashMap<>() {
		{
			put(10, TestStatus.FAILED);
			put(15, TestStatus.SKIPPED);
			put(20, TestStatus.SKIPPED);
			put(30, TestStatus.FAILED);
			put(35, TestStatus.SKIPPED);
			put(40, TestStatus.FAILED);
			put(45, TestStatus.FAILED);
			put(50, TestStatus.FAILED);
			put(60, TestStatus.FAILED);
			put(70, TestStatus.FAILED);
			put(80, TestStatus.FAILED);
			put(90, TestStatus.PASSED);
		}
	};

	private static final Map<Integer, String> languageMap = new HashMap<>() {
		{
			put(1, "ANSI C");
			put(2, "Java");
			put(3, "C++");
			put(4, "Pascal");
			put(5, "C++11");
			put(6, "Python 3");
		}
	};

	@SneakyThrows
	public static AreteResponseDTO fetchResult(String userID, String problemID) {
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest problemDescription = HttpRequest.newBuilder()
				.uri(URI.create(String.format("https://uhunt.onlinejudge.org/api/p/num/%s", problemID)))
				.GET()
				.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.build();

		String problem = client.send(problemDescription, HttpResponse.BodyHandlers.ofString()).body();
		String problemName = new JSONObject(problem).getString("title");

		HttpRequest studentSubmissions = HttpRequest.newBuilder()
				.uri(URI.create(String.format("https://uhunt.onlinejudge.org/api/subs-nums/%s/%s", userID, problemID)))
				.GET()
				.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.build();

		String student = client.send(studentSubmissions, HttpResponse.BodyHandlers.ofString()).body();
		JSONObject jsonObject = new JSONObject(student);
		JSONArray subs = jsonObject.getJSONObject(userID).getJSONArray("subs");
		List<UnitTestDTO> unitTests = new ArrayList<>();

		long minimumSubmissionTime = Math.toIntExact(System.currentTimeMillis() / 1000);
		long maximumSubmissionTime = 0;
		int passed = 0;

		for (int i = 0; i < subs.length(); i++) {
			JSONArray array = subs.getJSONArray(i);
			int submissionID = array.getInt(0);
			int verdictID = array.getInt(2);
			Long timeSpent = array.getLong(3);
			int submissionTime = array.getInt(4);
			Integer languageID = array.getInt(5);

			minimumSubmissionTime = Math.min(minimumSubmissionTime, submissionTime);
			maximumSubmissionTime = Math.max(maximumSubmissionTime, submissionTime);

			if (verdictID == 90) {
				passed += 1;
			}

			unitTests.add(UnitTestDTO.builder()
					.name(languageMap.getOrDefault(languageID, "Unknown language") +
							" - " + submissionID + " - " +
							verdictMap.getOrDefault(verdictID, "Unknown verdict"))
					.printStackTrace(false)
					.printExceptionMessage(false)
					.timeElapsed(timeSpent)
					.weight(verdictID == 90 ? 1 : 0)
					.status(verdictMapToResult.getOrDefault(verdictID, TestStatus.SKIPPED))
					.build());
		}

		TestContextDTO context = TestContextDTO.builder()
				.startDate(minimumSubmissionTime * 1000)
				.endDate(maximumSubmissionTime * 1000)
				.file(problemID + " - " + problemName)
				.grade(passed == 0 ? 0.0 : 100.0)
				.name(problemID + " - " + problemName)
				.passedCount(passed)
				.unitTests(unitTests)
				.weight(Math.max(passed, 1))
				.build();

		return AreteResponseDTO.builder()
				.testSuites(List.of(context))
				.build();
	}

}
