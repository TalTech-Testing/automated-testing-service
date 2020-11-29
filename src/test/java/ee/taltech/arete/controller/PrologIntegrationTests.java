package ee.taltech.arete.controller;

import ee.taltech.arete.java.request.AreteRequestDTO;
import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete_testing_service.AreteApplication;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static ee.taltech.arete.initializers.SubmissionInitializer.assertFullSubmission;
import static ee.taltech.arete.initializers.SubmissionInitializer.getFullSubmissionStringProlog;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = AreteApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PrologIntegrationTests {

	@LocalServerPort
	private int port;

	@Before
	public void init() {
		RestAssured.defaultParser = Parser.JSON;
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	@BeforeEach
	@SneakyThrows
	public void beforeEach() {
		TimeUnit.SECONDS.sleep(30);
	}

	@Test
	@Ignore
	public void addNewSubmissionProlog() {

		AreteRequestDTO payload = getFullSubmissionStringProlog(String.format("http://localhost:%s", port));
		AreteResponseDTO submission = given()
				.when()
				.body(payload)
				.post("/test")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponseDTO.class);

		assertFullSubmission(submission);

		//TODO To actually check if it tests
		// I dont have access to prolog tests

	}
}
