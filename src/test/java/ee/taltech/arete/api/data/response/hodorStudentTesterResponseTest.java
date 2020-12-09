package ee.taltech.arete.api.data.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete.java.response.hodor_studenttester.HodorStudentTesterResponse;
import ee.taltech.arete_testing_service.AreteApplication;
import ee.taltech.arete_testing_service.service.JobRunnerService;
import ee.taltech.arete_testing_service.service.hodor.HodorParser;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AreteApplication.class)
public class hodorStudentTesterResponseTest {

	private final static String home = System.getenv().getOrDefault("ARETE_HOME", System.getenv("HOME") + "/arete");

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JobRunnerService jobRunnerService;

	@Test
	public void FsharpParsingFullResponse() throws IOException {
		// given
		String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/fsharp-arete.json");

		// when
		AreteResponseDTO areteResponse = jobRunnerService.getAreteResponse(json);

		// then
		assertSuccessfulParsing(areteResponse);
	}


	@Test
	public void JavaParsingFullResponse() throws IOException {
		// given
		String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/java2.json");
		HodorStudentTesterResponse response = objectMapper.readValue(json, HodorStudentTesterResponse.class);

		// when
		AreteResponseDTO areteResponse = HodorParser.parse(response);

		// then
		assertSuccessfulParsing(areteResponse);
	}

	@Test
	public void JavaExamParsingParsesWithoutExceptionsAndFillsRequiredFields() throws IOException {
		// given
		String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/java.json");
		HodorStudentTesterResponse response = objectMapper.readValue(json, HodorStudentTesterResponse.class);

		// when
		AreteResponseDTO areteResponse = HodorParser.parse(response);

		// then
		assertSuccessfulParsing(areteResponse);
	}

	@Test
	public void JavaExamParsingParsesArete() throws IOException {
		// given
		String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/java-arete.json");
		AreteResponseDTO response = jobRunnerService.getAreteResponse(json);

		// then
		assertSuccessfulParsing(response);
	}

	@Test
	public void JavaDoesntCompileParsingParsesArete() throws IOException {
		// given
		String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/java-failed-arete.json");
		AreteResponseDTO response = jobRunnerService.getAreteResponse(json);

		// then
		assertSuccessfulParsing(response);
	}

	@Test
	public void PythonParsingParsesArete() throws IOException {
		// given
		String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/python-arete.json");
		AreteResponseDTO response = jobRunnerService.getAreteResponse(json);

		// then
		assertSuccessfulParsing(response);
	}

	@Test
	public void JavaDoesntCompile() throws IOException {
		// given
		String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/java3.json");
		HodorStudentTesterResponse response = objectMapper.readValue(json, HodorStudentTesterResponse.class);

		// when
		AreteResponseDTO areteResponse = HodorParser.parse(response);

		// then
		assertSuccessfulParsing(areteResponse);
	}

	@Test
	public void PythonParsing() throws IOException {
		// given
		String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/python.json");
		HodorStudentTesterResponse response = objectMapper.readValue(json, HodorStudentTesterResponse.class);

		// when
		AreteResponseDTO areteResponse = HodorParser.parse(response);

		// then
		assertSuccessfulParsing(areteResponse);
	}

	private String getJavaJson(String s) throws IOException {
		return Files.readString(Paths.get(home + s), StandardCharsets.UTF_8);
	}

	@SneakyThrows
	private void assertSuccessfulParsing(AreteResponseDTO areteResponse) {
		assert areteResponse.getErrors() != null;
		assert areteResponse.getFiles() != null;
		assert areteResponse.getTestFiles() != null;
		assert areteResponse.getSystemExtra() != null;
		assert areteResponse.getTestSuites() != null;
		assert areteResponse.getType().equals("arete");
	}
}
