package ee.taltech.arete.api.data.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import ee.taltech.arete.api.data.request.AreteRequest;
import ee.taltech.arete.api.data.request.AreteTestUpdate;
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

    private static void getJsonSchemaForResponse() throws IOException {

//		ObjectMapper mapper = new ObjectMapper();
//		JsonSchema schema = mapper.generateJsonSchema(AreteResponse.class);
//		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));

        ObjectMapper jacksonObjectMapper = new ObjectMapper();
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(jacksonObjectMapper);
        JsonSchema schema = schemaGen.generateSchema(AreteResponse.class);
        String schemaString = jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println(schemaString);


    }

    private static void getJsonSchemaForAreteRequest() throws IOException {

//		ObjectMapper mapper = new ObjectMapper();
//		JsonSchema schema = mapper.generateJsonSchema(AreteResponse.class);
//		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));

        ObjectMapper jacksonObjectMapper = new ObjectMapper();
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(jacksonObjectMapper);
        JsonSchema schema = schemaGen.generateSchema(AreteRequest.class);
        String schemaString = jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println(schemaString);

	}

	private static void getJsonSchemaForUpdateTest() throws IOException {

//		ObjectMapper mapper = new ObjectMapper();
//		JsonSchema schema = mapper.generateJsonSchema(AreteResponse.class);
//		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));

		ObjectMapper jacksonObjectMapper = new ObjectMapper();
		JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(jacksonObjectMapper);
		JsonSchema schema = schemaGen.generateSchema(AreteTestUpdate.class);
		String schemaString = jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
		System.out.println(schemaString);


	}

	public static void main(String[] args) throws IOException {
//        getJsonSchemaForResponse();
//        getJsonSchemaForAreteRequest();
//        getJsonSchemaForUpdateTest();
    }

	@Test
	public void JavaParsingFullResponse() throws IOException {
        // given
        String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/java2.json");
        hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
        Submission test = getFullSubmissionJava();

        // when
        AreteResponse areteResponse = new AreteResponse("ex", test, response);

        // then
        assertSuccessfulParsing(test, areteResponse);
    }

	@Test
    public void JavaExamParsingParsesWithoutExceptionsAndFillsRequiredFields() throws IOException {
        // given
        String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/java.json");
        hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
        Submission test = getFullSubmissionJava();

        // when
        AreteResponse areteResponse = new AreteResponse("ex", test, response);

        // then
        assertSuccessfulParsing(test, areteResponse);
        System.out.println(objectMapper.writeValueAsString(areteResponse));
    }

    @Test
    public void JavaDoesntCompile() throws IOException {
        // given
        String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/java3.json");
        hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
        Submission test = getFullSubmissionJava();

        // when
        AreteResponse areteResponse = new AreteResponse("ex", test, response);

        // then
        assertSuccessfulParsing(test, areteResponse);
    }

    private String getJavaJson(String s) throws IOException {
        return Files.readString(Paths.get(home + s), StandardCharsets.UTF_8);
    }

    @Test
    public void PythonParsing() throws IOException {
        // given
        String json = getJavaJson("/src/test/java/ee/taltech/arete/api/data/response/python.json");
        hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
        Submission test = getFullSubmissionJava();

        // when
        AreteResponse areteResponse = new AreteResponse("ex", test, response);

        // then
        assertSuccessfulParsing(test, areteResponse);
    }

    private void assertSuccessfulParsing(Submission test, AreteResponse areteResponse) {
        assert areteResponse.getOutput() != null;
        assert areteResponse.getErrors() != null;
        assert areteResponse.getConsoleOutputs() != null;
        assert areteResponse.getFiles() != null;
        assert areteResponse.getTestFiles() != null;
    }
}
