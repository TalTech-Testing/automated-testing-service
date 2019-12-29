package ee.taltech.arete.api.data.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.api.data.response.hodor_studenttester.hodorStudentTesterResponse;
import ee.taltech.arete.domain.Submission;
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

import static ee.taltech.arete.initializers.SubmissionInitializer.getFullSubmissionJava;

@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest
public class hodorStudentTesterResponseTest {

	private final static String home = System.getenv().getOrDefault("ARETE_HOME", System.getenv("HOME") + "/arete");

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void JavaParsingFullResponse() throws IOException {
		String json = Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/api/data/response/java2.json"), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
//		System.out.println(objectMapper.writeValueAsString(response));
		Submission test = getFullSubmissionJava();
		AreteResponse areteResponse = new AreteResponse(test, response);
		assert test.getResponse().size() > 0;
		assert areteResponse.getOutput() != null;
		assert areteResponse.getErrors() != null;
		assert areteResponse.getConsoleOutputs() != null;
		assert areteResponse.getFiles() != null;
		assert areteResponse.getTestFiles() != null;
		assert areteResponse.getId() >= 0;
//		System.out.println(objectMapper.writeValueAsString(test.getResponse()));
//		System.out.println(objectMapper.writeValueAsString(areteResponse.getOutput()));
	}

	private static void getJsonSchema() throws IOException {

//		ObjectMapper mapper = new ObjectMapper();
//		JsonSchema schema = mapper.generateJsonSchema(AreteResponse.class);
//		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));

		ObjectMapper jacksonObjectMapper = new ObjectMapper();
		JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(jacksonObjectMapper);
		JsonSchema schema = schemaGen.generateSchema(AreteResponse.class);
		String schemaString = jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
		System.out.println(schemaString);


	}

	public static void main(String[] args) throws IOException {
		getJsonSchema();
	}

	@Test
	public void JavaExamParsing() throws IOException {
		String json = Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/api/data/response/java.json"), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
		Submission test = getFullSubmissionJava();
		AreteResponse areteResponse = new AreteResponse(test, response);
		assert test.getResponse().size() > 0;
		assert areteResponse.getOutput() != null;
		assert areteResponse.getErrors() != null;
		assert areteResponse.getConsoleOutputs() != null;
		assert areteResponse.getFiles() != null;
		assert areteResponse.getTestFiles() != null;
		assert areteResponse.getId() >= 0;
//		System.out.println(objectMapper.writeValueAsString(test.getResponse()));
//		System.out.println(objectMapper.writeValueAsString(areteResponse.getOutput()));
	}

	@Test
	public void JavaDoesntCompile() throws IOException {
		String json = Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/api/data/response/java3.json"), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
		Submission test = getFullSubmissionJava();
		AreteResponse areteResponse = new AreteResponse(test, response);
		assert test.getResponse().size() > 0;
		assert areteResponse.getOutput() != null;
		assert areteResponse.getErrors() != null;
		assert areteResponse.getConsoleOutputs() != null;
		assert areteResponse.getFiles() != null;
		assert areteResponse.getTestFiles() != null;
		assert areteResponse.getId() >= 0;
//		System.out.println(objectMapper.writeValueAsString(test.getResponse()));
//		System.out.println(objectMapper.writeValueAsString(areteResponse.getOutput()));
	}

	@Test
	public void PythonParsing() throws IOException {
		String json = Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/api/data/response/python.json"), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
//		System.out.println(objectMapper.writeValueAsString(response));
		Submission test = getFullSubmissionJava();
		AreteResponse areteResponse = new AreteResponse(test, response);
		assert test.getResponse().size() > 0;
		assert areteResponse.getOutput() != null;
		assert areteResponse.getErrors() != null;
		assert areteResponse.getConsoleOutputs() != null;
		assert areteResponse.getFiles() != null;
		assert areteResponse.getTestFiles() != null;
		assert areteResponse.getId() >= 0;
//		System.out.println(objectMapper.writeValueAsString(test.getResponse()));
//		System.out.println(objectMapper.writeValueAsString(areteResponse.getOutput()));
	}
}
