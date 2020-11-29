package ee.taltech.arete.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete_testing_service.AreteApplication;
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

import java.util.concurrent.TimeUnit;

import static ee.taltech.arete.initializers.SubmissionInitializer.getFullSubmissionUva;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = AreteApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UvaIntegrationTests {

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
	public void uvaGetAsAreteResponse() {
		AreteResponseDTO response = given()
				.when()
				.body(getFullSubmissionUva(String.format("http://localhost:%s", port)))
				.post(":testSync")
				.then()
				.statusCode(is(HttpStatus.SC_ACCEPTED))
				.extract()
				.body()
				.as(AreteResponseDTO.class);

		assertEquals(100, response.getStyle());
		assertEquals(100.0, response.getTotalGrade());
	}
}
