package ee.taltech.arete.controller;

import ee.taltech.arete.java.request.AreteRequestDTO;
import ee.taltech.arete_testing_service.AreteApplication;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

import static ee.taltech.arete.initializers.SubmissionInitializer.getNormalSyncRequest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AreteApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GeneralIntegrationTests {

    @LocalServerPort
    private int port;

    @Before
    public void init() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    public void addNewSubmissionSyncBadRequestWrongTesterRepoFails() {

        AreteRequestDTO badSubmission = getNormalSyncRequest();
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

        AreteRequestDTO badSubmission = getNormalSyncRequest();
        badSubmission.setSource(new ArrayList<>());
        badSubmission.setTestSource(new ArrayList<>());

        given()
                .when()
                .body(badSubmission)
                .post(":testSync")
                .then()
                .statusCode(is(HttpStatus.SC_BAD_REQUEST));

    }
}