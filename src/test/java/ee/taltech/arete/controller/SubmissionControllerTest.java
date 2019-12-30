package ee.taltech.arete.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.AreteApplication;
import ee.taltech.arete.api.data.request.AreteRequestAsync;
import ee.taltech.arete.api.data.request.AreteRequestSync;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.domain.Submission;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static ee.taltech.arete.initializers.SubmissionInitializer.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;


@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = AreteApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubmissionControllerTest {

	private AreteRequestSync submission;
	private AreteRequestSync submissionNoStd;
	private AreteRequestSync submissionNoTester;
	private AreteRequestSync submissionNoStyle;

	@Autowired
	private ObjectMapper objectMapper;

	@LocalServerPort
	private int port;

	@Before
	public void init() throws IOException {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
		submission = getFullSubmissionStringSync(String.format("http://localhost:%s", port));
		submissionNoStd = getFullSubmissionStringPythonSyncNoStdout(String.format("http://localhost:%s", port));
		submissionNoTester = getFullSubmissionStringPythonSyncNoTesterFiles(String.format("http://localhost:%s", port));
		submissionNoStyle = getFullSubmissionStringPythonSyncNoStyle(String.format("http://localhost:%s", port));
	}

	@Test
	public void addNewSubmissionAsync() throws InterruptedException {

		AreteRequestAsync payload = getFullSubmissionStringControllerEndpoint();
		Submission submission = given()
				.when()
				.body(payload)
				.post("/test")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(Submission.class);

		TimeUnit.SECONDS.sleep(10);
		assertFullSubmission(submission);

		//TODO To actually check if it tests

	}


	@Test
	public void addNewSubmissionAsyncPython() throws InterruptedException {

		AreteRequestAsync payload = getFullSubmissionStringControllerEndpointPython();
		Submission submission = given()
				.when()
				.body(payload)
				.post("/test")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(Submission.class);

		TimeUnit.SECONDS.sleep(10);
		assertFullSubmission(submission);

		//TODO To actually check if it tests

	}


	@Test
	public void addNewSubmissionAsyncExam() throws InterruptedException {

		AreteRequestAsync payload = getFullSubmissionStringExamControllerEndpoint();
		Submission submission = given()
				.when()
				.body(payload)
				.post("/test")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(Submission.class);

		TimeUnit.SECONDS.sleep(10);
		assertFullSubmission(submission);

		//TODO To actually check if it tests

	}

	@Test
	public void addNewSubmissionSync() {

		AreteResponse answer = given()
				.when()
				.body(submission)
				.post("/test/sync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assert answer.getOutput() != null;
	}

	@Test
	public void addNewSubmissionSyncNoTests() {

		AreteResponse answer = given()
				.when()
				.body(submissionNoTester)
				.post("/test/sync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assert answer.getTestFiles().size() == 0;

	}


	@Test
	public void addNewSubmissionSyncNoStd() {

		AreteResponse answer = given()
				.when()
				.body(submissionNoStd)
				.post("/test/sync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assert answer.getConsoleOutputs().size() == 0;

	}


	@Test
	public void addNewSubmissionSyncNoStyle() {

		AreteResponse answer = given()
				.when()
				.body(submissionNoStyle)
				.post("/test/sync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assert answer.getStyle() == 100;

	}

	@Test
	public void updateImage() {
		given()
				.when()
				.post("/image/update/java-tester")
//				.post("/tester/update/python-tester")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED));

		// TODO check if image came to be
	}

	@Test
	public void updateTests() {
		given()
				.when()
				.post("/tests/update/ex/iti0102-2019")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED));

		// TODO check if folder came to me
	}
}
