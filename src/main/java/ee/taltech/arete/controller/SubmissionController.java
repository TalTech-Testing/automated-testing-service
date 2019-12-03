package ee.taltech.arete.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.RequestFormatException;
import ee.taltech.arete.service.docker.ImageCheck;
import ee.taltech.arete.service.git.GitPullService;
import ee.taltech.arete.service.queue.PriorityQueueService;
import ee.taltech.arete.service.submission.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class SubmissionController {
	private static Logger LOGGER = LoggerFactory.getLogger(SubmissionController.class);

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SubmissionService submissionService;

	@Autowired
	private PriorityQueueService priorityQueueService;

	@Autowired
	private GitPullService gitPullService;

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/test")
	public Submission Test(HttpEntity<String> httpEntity) {
		String requestBody = httpEntity.getBody();
		LOGGER.info("Parsing request body: " + requestBody);
		if (requestBody == null) throw new RequestFormatException("Empty input!");

		try {
			Submission submission = objectMapper.readValue(requestBody, Submission.class);
			submissionService.populateFields(submission);
			submissionService.saveSubmission(submission);
			priorityQueueService.enqueue(submission);
			return submission;

		} catch (JsonProcessingException e) {
			LOGGER.error("Request format invalid!", e);
			throw new RequestFormatException(e.getMessage(), e);

		}
	}


	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/test/sync")
	public void TestSync(HttpEntity<String> httpEntity) {
		String requestBody = httpEntity.getBody();
		LOGGER.info("Parsing request body: " + requestBody);
		if (requestBody == null) throw new RequestFormatException("Empty input!");

		// TODO: codera
//		try {
//			Submission submission = objectMapper.readValue(requestBody, Submission.class);
//			submissionService.populateFields(submission);
//			submissionService.saveSubmission(submission);
//			priorityQueueService.enqueue(submission);
//			return submission;
//
//		} catch (JsonProcessingException e) {
//			LOGGER.error("Request format invalid!", e);
//			throw new RequestFormatException(e.getMessage(), e);
//
//		}
	}


	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/image/update/{image}")
	public void UpdateImage(@PathVariable("image") String image) throws InterruptedException {

		String dockerHost = System.getenv().getOrDefault("DOCKER_HOST", "unix:///var/run/docker.sock");

		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost(dockerHost)
				.withDockerTlsVerify(false)
				.build();

		new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/" + image).pull();

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/tests/update/{projectBase}/{project}")
	public void UpdateTests(@PathVariable("projectBase") String projectBase, @PathVariable("project") String project) {

		String pathToTesterFolder = String.format("tests/%s/", project);
		String pathToTesterRepo = String.format("https://gitlab.cs.ttu.ee/%s/%s.git", project, projectBase);
		LOGGER.info("Checking for update for tester:");
		gitPullService.pullOrClone(pathToTesterFolder, pathToTesterRepo, Optional.empty());

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/submissions/{hash}")
	public List<Submission> GetSubmissionsByHash(@PathVariable("hash") String hash) {

		return submissionService.getSubmissionByHash(hash);

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/submissions")
	public List<Submission> GetSubmissions() {

		return submissionService.getSubmissions();

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/submissions/{hash}/logs")
	public String GetSubmissionLogs(@PathVariable("hash") String hash) {

		return "<!DOCTYPE html>\n" +
				"<html lang=\"en\">\n" +
				"<head>\n" +
				"    <meta charset=\"UTF-8\">\n" +
				"    <title>Logs</title>\n" +
				"</head>\n" +
				"<body>" +
				submissionService.getSubmissionByHash(hash).stream()
						.map(submission -> submission.getResultTest()
								.replace("\n", "<br />\n"))
						.collect(Collectors.joining()) +
				"</body>\n" +
				"</html>";

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/logs")
	public String GetLogs() {

		try {
			return "<!DOCTYPE html>\n" +
					"<html lang=\"en\">\n" +
					"<head>\n" +
					"    <meta charset=\"UTF-8\">\n" +
					"    <title>Logs</title>\n" +
					"</head>\n" +
					"<body>" +
					Files.readString(Paths.get("logs/spring.log")).replace("\n", "<br />\n") +
					"</body>\n" +
					"</html>";

		} catch (Exception e) {
			throw new RequestFormatException(e.getMessage());
		}
	}

}
