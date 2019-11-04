package ee.taltech.arete.controller;

import ee.taltech.arete.AreteApplication;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.repository.SubmissionRepository;
import ee.taltech.arete.service.SubmissionService;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = AreteApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionControllerTest {

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
    public void addNewSubmission() throws IOException {

        String payload = String.format("{\"uniid\": %s,\"hash\": %s,\"testingPlatform\": \"%s.\", \"returnUrl\": \"%s.\", \"extra\": \"%s.\", }", "envomp", "hash", "python", "neti.ee", "[\"style\"]");

        given()
                .when()
                .body(payload)
                .post("/test/hash")
                .then()
                .statusCode(is(HttpStatus.SC_ACCEPTED));

    }

}