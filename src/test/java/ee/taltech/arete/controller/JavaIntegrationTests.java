package ee.taltech.arete.controller;

		import com.fasterxml.jackson.databind.ObjectMapper;
		import ee.taltech.arete.AreteApplication;
		import ee.taltech.arete.api.data.request.AreteRequest;
		import ee.taltech.arete.api.data.response.arete.AreteResponse;
		import io.restassured.RestAssured;
		import io.restassured.parsing.Parser;
		import org.apache.http.HttpStatus;
		import org.junit.Before;
		import org.junit.Test;
		import org.junit.runner.RunWith;
		import org.springframework.beans.factory.annotation.Autowired;
		import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
		import org.springframework.boot.test.context.SpringBootTest;
		import org.springframework.boot.web.server.LocalServerPort;
		import org.springframework.test.context.junit4.SpringRunner;

		import java.util.ArrayList;

		import static ee.taltech.arete.initializers.SubmissionInitializer.*;
		import static io.restassured.RestAssured.given;
		import static org.hamcrest.Matchers.is;
		import static org.junit.jupiter.api.Assertions.assertEquals;
		import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = AreteApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JavaIntegrationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@LocalServerPort
	private int port;

	@Before
	public void init() {
		RestAssured.defaultParser = Parser.JSON;
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	@Test
	public void addNewSubmissionSyncReturnsOutput() {

		AreteResponse response = given()
				.when()
				.body(getFullSubmissionStringSync(String.format("http://localhost:%s", port)))
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assertFullSubmission(response);
		assertEquals(23, response.getTotalCount());
		assertEquals(23, response.getTotalPassedCount());
	}

	@Test
	public void addNewSubmissionSyncBadRequestWrongTestingPlatformFails() {

		AreteRequest badSubmission = getFullSubmissionStringSyncBadRequest(String.format("http://localhost:%s", port));
		badSubmission.setTestingPlatform("SupriseMe");

		AreteResponse answer = given()
				.when()
				.body(badSubmission)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assertTrue(answer.getFailed());
	}

	@Test
	public void addNewSubmissionSyncBadRequestWrongStudentRepoFails() {

		AreteRequest badSubmission = getFullSubmissionStringSyncBadRequest(String.format("http://localhost:%s", port));
		badSubmission.setGitStudentRepo("https://www.neti.ee/");

		AreteResponse answer = given()
				.when()
				.body(badSubmission)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assertTrue(answer.getFailed());
	}

	@Test
	public void addNewSubmissionSyncBadRequestWrongTesterRepoFails() {

		AreteRequest badSubmission = getFullSubmissionStringSyncBadRequest(String.format("http://localhost:%s", port));
		badSubmission.setGitTestSource("https://www.neti.ee/");

		AreteResponse answer = given()
				.when()
				.body(badSubmission)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assertTrue(answer.getFailed());
	}

	@Test
	public void addNewSubmissionSyncNoFilesFails() {

		AreteRequest badSubmission = getFullSubmissionStringSyncBadRequest(String.format("http://localhost:%s", port));
		badSubmission.setSource(new ArrayList<>());
		badSubmission.setTestSource(new ArrayList<>());

		AreteResponse answer = given()
				.when()
				.body(badSubmission)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assertTrue(answer.getFailed());
	}
}
