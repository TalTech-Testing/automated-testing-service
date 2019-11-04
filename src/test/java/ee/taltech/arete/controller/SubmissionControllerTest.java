package ee.taltech.arete.controller;

import ee.taltech.arete.AreteApplication;
import ee.taltech.arete.domain.Submission;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static ee.taltech.arete.controller.SubmissionInit.assertFullSubmission;
import static ee.taltech.arete.controller.SubmissionInit.getFullSubmission;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;


@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = AreteApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubmissionControllerTest {

	private Submission submission;

	@LocalServerPort
	private int port;

	@Before
	public void init() {
		submission = new Submission("envomp", "hash", "python", "neti.ee", new String[]{"style"});
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	@Test
	public void addNewSubmission() throws IOException, JSONException {

		String payload = getFullSubmission();
		Submission submission = given()
				.when()
				.body(payload)
				.post("/test/hash")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(Submission.class);

		assertFullSubmission(submission);

	}
}