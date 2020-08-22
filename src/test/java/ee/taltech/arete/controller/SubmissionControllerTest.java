package ee.taltech.arete.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ee.taltech.arete.AreteApplication;
import ee.taltech.arete.api.data.request.AreteRequest;
import ee.taltech.arete.api.data.request.AreteTestUpdate;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
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
import static org.junit.jupiter.api.Assertions.*;


@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = AreteApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubmissionControllerTest {

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
	public void addNewSubmissionSyncReturnsFullSubmission() {

		AreteRequest payload = getFullSubmissionStringControllerEndpoint(String.format("http://localhost:%s", port));
		AreteResponse response = given()
				.when()
				.body(payload)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		// then
		assertFullSubmission(response);
		assertEquals(23, response.getTotalCount());
		assertEquals(19, response.getTotalPassedCount());
	}


	@Test
	public void addNewSubmissionSyncPythonReturnsFullSubmission() {

		AreteRequest payload = getFullSubmissionStringControllerEndpointPython(String.format("http://localhost:%s", port));
		AreteResponse response = given()
				.when()
				.body(payload)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		// then
		assertFullSubmission(response);
		assertEquals(18, response.getTotalCount());
		assertEquals(18, response.getTotalPassedCount());
	}

	@Test
	public void addNewSubmissionSyncPythonBigReturnsFullSubmission() {

		AreteRequest payload = getFullSubmissionStringControllerEndpointPythonLong(String.format("http://localhost:%s", port));
		AreteResponse response = given()
				.when()
				.body(payload)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		// then
		assertFullSubmission(response);
		assertEquals(21, response.getTotalCount());
		assertEquals(13, response.getTotalPassedCount());
	}


	@Test
	public void addNewSubmissionSyncPythonRecursionReturnsOutputEmail() {
		AreteRequest payload = getFullSubmissionStringControllerEndpointPythonRecursion(String.format("http://localhost:%s", port));
		AreteResponse response = given()
				.when()
				.body(payload)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		// then
		assertFullSubmission(response);
		assertEquals(1, response.getTotalCount());
		assertEquals(0, response.getTotalPassedCount());
		assertEquals("envomp@ttu.ee", response.getEmail());
	}

	@Test
	public void addNewSubmissionSyncPythonCustomConfigurationReturnsFullSubmission() {
		AreteRequest payload = getFullSubmissionStringControllerEndpointPythonCustomConfiguration(String.format("http://localhost:%s", port));
		AreteResponse response = given()
				.when()
				.body(payload)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		//then
		assertFullSubmission(response);
		assertEquals(18, response.getTotalCount());
		assertEquals(18, response.getTotalPassedCount());
		assertEquals("envomp@ttu.ee", response.getEmail());
	}

	@Test
	public void addNewSubmissionSyncExamReturnsFullSubmission() {

		AreteRequest payload = getFullSubmissionStringExamControllerEndpoint(String.format("http://localhost:%s", port));
		AreteResponse response = given()
				.when()
				.body(payload)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		//then
		assertFullSubmission(response);
		assertEquals(55, response.getTotalCount());
		assertEquals(46, response.getTotalPassedCount());
		assertEquals("envomp@ttu.ee", response.getEmail());
	}

	@Test
	public void addNewSubmissionSyncExamReturnsOutputAndReturnExtra() {
		ObjectNode root = objectMapper.createObjectNode();
		root.put("some", "stuff");
		AreteRequest submission = getFullSubmissionStringExamControllerEndpoint(String.format("http://localhost:%s", port));
		submission.setReturnExtra(root);

		JsonNode response = given()
				.when()
				.body(submission)
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(JsonNode.class);
		// then
		assertNotNull(response.get("output"));
		assertNotNull(response.get("returnExtra"));
	}


	@Test
	@Ignore
	public void addNewSubmissionProlog() {

		AreteRequest payload = getFullSubmissionStringProlog(String.format("http://localhost:%s", port));
		AreteResponse submission = given()
				.when()
				.body(payload)
				.post("/test")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assertFullSubmission(submission);

		//TODO To actually check if it tests
		// I dont have access to prolog tests

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
	public void addNewSubmissionSyncNoTestsDoesntReturnTestFiles() {

		AreteResponse response = given()
				.when()
				.body(getFullSubmissionStringPythonSyncNoTesterFiles(String.format("http://localhost:%s", port)))
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assertEquals(0, response.getTestFiles().size());
	}


	@Test
	public void addNewSubmissionSyncNoStdReturnsNoStd() {

		AreteResponse response = given()
				.when()
				.body(getFullSubmissionStringPythonSyncNoStdout(String.format("http://localhost:%s", port)))
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assertEquals(0, response.getConsoleOutputs().size());
		assertFullSubmission(response);
		assertEquals(23, response.getTotalCount());
		assertEquals(16, response.getTotalPassedCount());

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

	@Test
	public void addNewSubmissionSyncNoStyleReturnsStyle100() {

		AreteResponse response = given()
				.when()
				.body(getFullSubmissionStringPythonSyncNoStyle(String.format("http://localhost:%s", port)))
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponse.class);

		assertEquals(100, response.getStyle());
		assertFullSubmission(response);
		assertEquals(23, response.getTotalCount());
		assertEquals(16, response.getTotalPassedCount());
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
		AreteTestUpdate update = new AreteTestUpdate(
				new AreteTestUpdate.Project("https://gitlab.cs.ttu.ee/iti0102-2019/ex.git", "iti0102-2019/ex", "iti0102-2019"),
				new ArrayList<>());
		given()
				.body(update)
				.when()
				.put("/tests")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED));

	}
}
