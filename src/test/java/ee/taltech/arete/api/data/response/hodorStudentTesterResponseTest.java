package ee.taltech.arete.api.data.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.AreteApplication;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.api.data.response.hodor_studenttester.hodorStudentTesterResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static ee.taltech.arete.initializers.SubmissionInitializer.getFullSubmissionJava;
import static ee.taltech.arete.initializers.SubmissionInitializer.getFullSubmissionPython;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AreteApplication.class)
public class hodorStudentTesterResponseTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void JavaParsing() throws IOException {
		String json = Files.readString(Paths.get("src/test/java/ee/taltech/arete/api/data/response/java2.json").toAbsolutePath(), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
//		System.out.println(objectMapper.writeValueAsString(response));
		AreteResponse areteResponse = new AreteResponse(getFullSubmissionJava(), response);
//		System.out.println(objectMapper.writeValueAsString(areteResponse));
	}

	@Test
	public void JavaExamParsing() throws IOException {
		String json = Files.readString(Paths.get("rc/test/java/ee/taltech/arete/api/data/response/java.json").toAbsolutePath(), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
		AreteResponse areteResponse = new AreteResponse(getFullSubmissionJava(), response);
//		System.out.println(objectMapper.writeValueAsString(areteResponse));
	}

	@Test
	public void JavaDoesntCompile() throws IOException {
		String json = Files.readString(Paths.get("rc/test/java/ee/taltech/arete/api/data/response/java3.json").toAbsolutePath(), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
		AreteResponse areteResponse = new AreteResponse(getFullSubmissionJava(), response);
//		System.out.println(objectMapper.writeValueAsString(areteResponse));
	}

	@Test
	public void PythonParsing() throws IOException {
		String json = Files.readString(Paths.get("rc/test/java/ee/taltech/arete/api/data/response/python.json").toAbsolutePath(), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
//		System.out.println(objectMapper.writeValueAsString(response));
		AreteResponse areteResponse = new AreteResponse(getFullSubmissionPython(), response);
//		System.out.println(objectMapper.writeValueAsString(areteResponse));
	}
}
