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

	private final static String home = System.getenv().getOrDefault("ARETE_HOME", System.getenv("HOME") + "/arete");

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void JavaParsingFullResponse() throws IOException {
		String json = Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/api/data/response/java2.json"), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
//		System.out.println(objectMapper.writeValueAsString(response));
		AreteResponse areteResponse = new AreteResponse(getFullSubmissionJava(), response);
		System.out.println(objectMapper.writeValueAsString(areteResponse.getOutput()));
	}

	@Test
	public void JavaExamParsing() throws IOException {
		String json = Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/api/data/response/java.json"), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
		AreteResponse areteResponse = new AreteResponse(getFullSubmissionJava(), response);
		System.out.println(objectMapper.writeValueAsString(areteResponse.getOutput()));
	}

	@Test
	public void JavaDoesntCompile() throws IOException {
		String json = Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/api/data/response/java3.json"), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
		AreteResponse areteResponse = new AreteResponse(getFullSubmissionJava(), response);
		System.out.println(objectMapper.writeValueAsString(areteResponse.getOutput()));
	}

	@Test
	public void PythonParsing() throws IOException {
		String json = Files.readString(Paths.get(home + "/src/test/java/ee/taltech/arete/api/data/response/python.json"), StandardCharsets.UTF_8);
		hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
//		System.out.println(objectMapper.writeValueAsString(response));
		AreteResponse areteResponse = new AreteResponse(getFullSubmissionPython(), response);
		System.out.println(objectMapper.writeValueAsString(areteResponse.getOutput()));
	}
}
