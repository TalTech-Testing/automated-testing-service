package ee.taltech.arete.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
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

	private HashMap<String, AreteResponse> syncWaitingRoom = new HashMap<>();

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/test")
	public Submission Test(HttpEntity<String> httpEntity) {
		String requestBody = httpEntity.getBody();
		LOGGER.info("Parsing request body: " + requestBody);
		if (requestBody == null) throw new RequestFormatException("Empty input!");

		try {

			Submission submission = objectMapper.readValue(requestBody, Submission.class);
			submissionService.populateAsyncFields(submission);
			submissionService.saveSubmission(submission);
			priorityQueueService.enqueue(submission);
			return submission;

		} catch (JsonProcessingException e) {
			LOGGER.error("Request format invalid: {}", e.getMessage());
			throw new RequestFormatException(e.getMessage());

		}
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/test/sync")
	public AreteResponse TestSync(HttpEntity<String> httpEntity) {
		String requestBody = httpEntity.getBody();
		LOGGER.info("Parsing request body: " + requestBody);
		if (requestBody == null) throw new RequestFormatException("Empty input!");
		try {

			Submission submission = objectMapper.readValue(requestBody, Submission.class);
			String hash = submissionService.populateSyncFields(submission);
			submissionService.saveSubmission(submission);
			priorityQueueService.enqueue(submission);

			while (!syncWaitingRoom.containsKey(hash)) {
				TimeUnit.SECONDS.sleep(1);
			}
			return syncWaitingRoom.remove(hash);

		} catch (JsonProcessingException | InterruptedException e) {
			LOGGER.error("Request format invalid: {}", e.getMessage());
			throw new RequestFormatException(e.getMessage());
		}
	}


	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/waitingroom/{hash}")
	public void WaitingList(HttpEntity<String> httpEntity, @PathVariable("hash") String hash) {
		try {
			syncWaitingRoom.put(hash, objectMapper.readValue(Objects.requireNonNull(httpEntity.getBody()), AreteResponse.class));
		} catch (Exception e) {
			LOGGER.error("Processing sync job failed: {}", e.getMessage());
			syncWaitingRoom.put(hash, new AreteResponse("Codera", new Submission(), e.getMessage()));
		}
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/image/update/{image}")
	public String UpdateImage(@PathVariable("image") String image) {

		try {
			priorityQueueService.halt();
			String dockerHost = System.getenv().getOrDefault("DOCKER_HOST", "unix:///var/run/docker.sock");
			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost(dockerHost)
					.withDockerTlsVerify(false)
					.build();
			new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/" + image).pull();
			priorityQueueService.go();
			return "Successfully updated image: " + image;
		} catch (Exception e) {
			throw new RequestFormatException(e.getMessage());
		}

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/tests/update")
	public String UpdateTests(HttpEntity<String> httpEntity) {

		try {
			String requestBody = httpEntity.getBody();
			LOGGER.info("Parsing request body: " + requestBody);
			if (requestBody == null) throw new RequestFormatException("Empty input!");
			Submission update = objectMapper.readValue(requestBody, Submission.class);
			submissionService.fixRepo(update);
			String[] url = update.getGitTestSource().split("[/:]");
			update.setCourse(url[url.length - 2]);

			priorityQueueService.halt();
			String pathToTesterFolder = String.format("tests/%s/", update.getCourse());
			String pathToTesterRepo = update.getGitTestSource();
			LOGGER.info("Checking for update for tester:");
			gitPullService.pullOrClone(pathToTesterFolder, pathToTesterRepo, Optional.empty());
			priorityQueueService.go();
			return "Successfully updated tests: " + update.getCourse();
		} catch (Exception e) {
			throw new RequestFormatException(e.getMessage());
		}

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/submissions/{hash}")
	public List<Submission> GetSubmissionsByHash(@PathVariable("hash") String hash) {

		try {
			return submissionService.getSubmissionByHash(hash);
		} catch (Exception e) {
			return new ArrayList<>();
		}

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/submissions")
	public List<Submission> GetSubmissions() {

		try {
			return submissionService.getSubmissions();
		} catch (Exception e) {
			return new ArrayList<>();
		}

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/submissions/active")
	public List<Submission> GetActiveSubmissions() {

		try {
			return priorityQueueService.getActiveSubmissions();
		} catch (Exception e) {
			return new ArrayList<>();
		}

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/submissions/{hash}/logs")
	public List<List<AreteResponse>> GetSubmissionLogs(@PathVariable("hash") String hash) {

		try {
			return submissionService.getSubmissionByHash(hash)
					.stream()
					.map(Submission::getResponse)
					.collect(Collectors.toList());
		} catch (Exception e) {
			return new ArrayList<>();
		}

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/debug/{bool}")
	public void setDebug(@PathVariable("bool") int bool) {

		try {
			submissionService.debugMode(bool != 0);
		} catch (Exception e) {
			throw new RequestFormatException(e.getMessage());
		}
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/logs")
	public String GetLogs() {

		try {
			return Files.readString(Paths.get("logs/spring.log"));
		} catch (Exception e) {
			throw new RequestFormatException(e.getMessage());
		}
	}
}
