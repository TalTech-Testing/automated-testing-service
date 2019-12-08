package ee.taltech.arete.controller;

import ee.taltech.arete.AreteApplication;
import ee.taltech.arete.api.data.request.AreteRequestAsync;
import ee.taltech.arete.api.data.request.AreteRequestSync;
import ee.taltech.arete.api.data.response.TestingResult;
import ee.taltech.arete.domain.Submission;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static ee.taltech.arete.initializers.SubmissionInitializer.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;


@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = AreteApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubmissionControllerTest {

	private AreteRequestSync submission;

	@LocalServerPort
	private int port;

	@Before
	public void init() throws IOException {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
		submission = getFullSubmissionStringSync(String.format("http://localhost:%s", port));
	}

	@Test
	public void addNewSubmissionAsync() throws InterruptedException {

		AreteRequestAsync payload = getFullSubmissionString();
		Submission submission = given()
				.when()
				.body(payload)
				.post("/test")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(Submission.class);

		assertFullSubmission(submission);

		TimeUnit.SECONDS.sleep(30);
		//TODO To actually check if it tests

	}

	@Test
	public void addNewSubmissionSync() {

		TestingResult answer = given()
				.when()
				.body(submission)
				.post("/test/sync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(TestingResult.class);

		System.out.println(answer);

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