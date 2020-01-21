package ee.taltech.arete.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.AreteApplication;
import ee.taltech.arete.api.data.request.AreteRequest;
import ee.taltech.arete.api.data.request.AreteTestUpdate;
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

	private AreteRequest submission;
	private AreteRequest submissionNoStd;
	private AreteRequest submissionNoTester;
	private AreteRequest submissionNoStyle;
	private AreteRequest submissionRecursion;
	private AreteRequest submissionSyncExam;

	@Autowired
	private ObjectMapper objectMapper;

	@LocalServerPort
	private int port;

	@Before
	public void init() throws IOException {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
		submissionSyncExam = getFullSubmissionStringExamControllerEndpoint(String.format("http://localhost:%s", port));
		submissionRecursion = getFullSubmissionStringControllerEndpointPythonRecursion(String.format("http://localhost:%s", port));
		submission = getFullSubmissionStringSync(String.format("http://localhost:%s", port));
		submissionNoStd = getFullSubmissionStringPythonSyncNoStdout(String.format("http://localhost:%s", port));
		submissionNoTester = getFullSubmissionStringPythonSyncNoTesterFiles(String.format("http://localhost:%s", port));
		submissionNoStyle = getFullSubmissionStringPythonSyncNoStyle(String.format("http://localhost:%s", port));
	}

	@Test
	public void addNewSubmissionAsyncReturnsFullSubmission() throws InterruptedException {

		AreteRequest payload = getFullSubmissionStringControllerEndpoint();
		Submission submission = given()
				.when()
				.body(payload)
				.post(":testAsync")
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
	public void addNewSubmissionAsyncPythonReturnsFullSubmission() throws InterruptedException {

		AreteRequest payload = getFullSubmissionStringControllerEndpointPython();
		Submission submission = given()
				.when()
				.body(payload)
				.post(":testAsync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(Submission.class);

		TimeUnit.SECONDS.sleep(10);
		assertFullSubmission(submission);

	}

	@Test
	public void addNewSubmissionAsyncPythonLFSReturnsFullSubmission() throws InterruptedException {

		AreteRequest payload = getFullSubmissionStringControllerEndpointPythonLong();
		Submission submission = given()
				.when()
				.body(payload)
				.post(":testAsync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(Submission.class);

		TimeUnit.SECONDS.sleep(10);
		assertFullSubmission(submission);
	}


	@Test
	public void addNewSubmissionSyncPythonRecursionReturnsOutput() {
		AreteResponse response = given()
				.when()
				.body(submissionRecursion)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assert response.getOutput() != null;
	}

	@Test
	public void addNewSubmissionAsyncPythonCustomConfigurationReturnsFullSubmission() throws InterruptedException {
		AreteRequest payload = getFullSubmissionStringControllerEndpointPythonCustomConfiguration();
		Submission submission = given()
				.when()
				.body(payload)
				.post(":testAsync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(Submission.class);

		assertFullSubmission(submission);

		TimeUnit.SECONDS.sleep(10);

	}

	@Test
	public void addNewSubmissionAsyncExamReturnsFullSubmission() throws InterruptedException {

		AreteRequest payload = getFullSubmissionStringExamControllerEndpoint();
		Submission submission = given()
				.when()
				.body(payload)
				.post(":testAsync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(Submission.class);

		TimeUnit.SECONDS.sleep(10);
		assertFullSubmission(submission);

	}

	@Test
	public void addNewSubmissionSyncExamReturnsOutput() {

		AreteResponse response = given()
				.when()
				.body(submissionSyncExam)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assert response.getOutput() != null;
	}


//	@Test
//	public void addNewSubmissionProlog() throws InterruptedException {
//
//		AreteRequest payload = getFullSubmissionStringProlog();
//		Submission submission = given()
//				.when()
//				.body(payload)
//				.post("/test")
//				.then()
//				.statusCode(is(HttpStatus.SC_ACCEPTED))
//				.extract()
//				.body()
//				.as(Submission.class);
//
//		TimeUnit.SECONDS.sleep(10);
//		assertFullSubmission(submission);
//
//		//TODO To actually check if it tests
//		// I dont have access to prolog tests
//
//	}

	@Test
	public void addNewSubmissionSyncReturnsOutput() {

		AreteResponse answer = given()
				.when()
				.body(submission)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assert answer.getOutput() != null;
	}

	@Test
	public void addNewSubmissionSyncNoTestsDoesntReturnTestFiles() {

		AreteResponse answer = given()
				.when()
				.body(submissionNoTester)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assert answer.getTestFiles().size() == 0;

	}


	@Test
	public void addNewSubmissionSyncNoStdReturnsNoStd() {

		AreteResponse answer = given()
				.when()
				.body(submissionNoStd)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assert answer.getConsoleOutputs().size() == 0;

	}


	@Test
	public void addNewSubmissionSyncNoStyleReturnsStyle100() {

		AreteResponse answer = given()
				.when()
				.body(submissionNoStyle)
				.post(":testSync")
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
				.put("/image/prolog-tester")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED));

	}

	@Test
	public void updateTests() {
		AreteTestUpdate update = new AreteTestUpdate(new AreteTestUpdate.Project("https://gitlab.cs.ttu.ee/iti0102-2019/ex.git", "iti0102-2019/ex"));
		given()
				.body(update)
				.when()
				.put("/tests")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED));

	}
}
