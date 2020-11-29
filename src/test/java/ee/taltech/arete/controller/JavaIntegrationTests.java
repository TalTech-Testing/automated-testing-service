package ee.taltech.arete.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete_testing_service.AreteApplication;
import ee.taltech.arete.java.request.AreteRequestDTO;
import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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

	@BeforeEach
	@SneakyThrows
	public void beforeEach() {
		TimeUnit.SECONDS.sleep(5);
	}

    @Test
    public void addNewSubmissionSyncReturnsOutput() {

        AreteResponseDTO response = given()
                .when()
                .body(getFullSubmissionStringSync(String.format("http://localhost:%s", port)))
                .post(":testSync")
                .then()
                .statusCode(is(HttpStatus.SC_ACCEPTED))
                .extract()
                .body()
                .as(AreteResponseDTO.class);

        assertFullSubmission(response);
        assertEquals(23, response.getTotalCount());
        assertEquals(23, response.getTotalPassedCount());
    }

    @Test
    public void addNewSubmissionSyncBadRequestWrongTestingPlatformFails() {

        AreteRequestDTO badSubmission = getFullSubmissionStringSyncBadRequest(String.format("http://localhost:%s", port));
        badSubmission.setTestingPlatform("SupriseMe");

        AreteResponseDTO answer = given()
                .when()
                .body(badSubmission)
                .post(":testSync")
                .then()
                .statusCode(is(HttpStatus.SC_ACCEPTED))
                .extract()
                .body()
                .as(AreteResponseDTO.class);

        assertTrue(answer.getFailed());
    }

    @Test
    public void addNewSubmissionSyncBadRequestWrongStudentRepoFails() {

        AreteRequestDTO badSubmission = getFullSubmissionStringSyncBadRequest(String.format("http://localhost:%s", port));
        badSubmission.setGitStudentRepo("https://www.neti.ee/");

        AreteResponseDTO answer = given()
                .when()
                .body(badSubmission)
                .post(":testSync")
                .then()
                .statusCode(is(HttpStatus.SC_ACCEPTED))
                .extract()
                .body()
                .as(AreteResponseDTO.class);

        assertTrue(answer.getFailed());
    }

    @Test
    public void addNewSubmissionSyncBadRequestWrongTesterRepoFails() {

        AreteRequestDTO badSubmission = getFullSubmissionStringSyncBadRequest(String.format("http://localhost:%s", port));
        badSubmission.setGitTestRepo("https://www.neti.ee/");

        given()
                .when()
                .body(badSubmission)
                .post(":testSync")
                .then()
                .statusCode(is(HttpStatus.SC_BAD_REQUEST));
    }

    @Test
    public void addNewSubmissionSyncNoFilesFails() {

        AreteRequestDTO badSubmission = getFullSubmissionStringSyncBadRequest(String.format("http://localhost:%s", port));
        badSubmission.setSource(new ArrayList<>());
        badSubmission.setTestSource(new ArrayList<>());

        given()
                .when()
                .body(badSubmission)
                .post(":testSync")
                .then()
                .statusCode(is(HttpStatus.SC_BAD_REQUEST));

    }


    @Test
    public void addNewSubmissionSyncReturnsFullSubmission() {

        AreteRequestDTO payload = getFullSubmissionStringControllerEndpoint(String.format("http://localhost:%s", port));
        AreteResponseDTO response = given()
                .when()
                .body(payload)
                .post(":testSync")
                .then()
                .statusCode(is(HttpStatus.SC_ACCEPTED))
                .extract()
                .body()
                .as(AreteResponseDTO.class);

        // then
        assertFullSubmission(response);
        assertEquals(23, response.getTotalCount());
        assertEquals(19, response.getTotalPassedCount());
    }
}
