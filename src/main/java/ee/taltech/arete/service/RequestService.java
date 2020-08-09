package ee.taltech.arete.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import ee.taltech.arete.api.data.request.AreteTestUpdate;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.domain.DefaultParameters;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.RequestFormatException;
import ee.taltech.arete.service.docker.ImageCheck;
import ee.taltech.arete.service.git.GitPullService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RequestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestService.class);

	private final ObjectMapper objectMapper;
	private final SubmissionService submissionService;
	private final PriorityQueueService priorityQueueService;
	private final GitPullService gitPullService;
	private final JobRunnerService jobRunnerService;
	private final HashMap<String, AreteResponse> syncWaitingRoom = new HashMap<>();

	public RequestService(ObjectMapper objectMapper, SubmissionService submissionService, PriorityQueueService priorityQueueService, GitPullService gitPullService, JobRunnerService jobRunnerService) {
		this.objectMapper = objectMapper;
		this.submissionService = submissionService;
		this.priorityQueueService = priorityQueueService;
		this.gitPullService = gitPullService;
		this.jobRunnerService = jobRunnerService;
	}

	public Submission testAsync(HttpEntity<String> httpEntity) {
		String requestBody = httpEntity.getBody();
		LOGGER.info("Parsing request body: " + requestBody);
		if (requestBody == null) throw new RequestFormatException("Empty input!");

		try {

			Submission submission = objectMapper.readValue(requestBody, Submission.class);
			submissionService.populateAsyncFields(submission);
			priorityQueueService.enqueue(submission);
			return submission;

		} catch (JsonProcessingException e) {
			LOGGER.error("Request format invalid: {}", e.getMessage());
			throw new RequestFormatException(e.getMessage());

		}

	}

	public AreteResponse testSync(HttpEntity<String> httpEntity) {
		String requestBody = httpEntity.getBody();
		LOGGER.info("Parsing request body: " + requestBody);
		if (requestBody == null) throw new RequestFormatException("Empty input!");

		try {

			Submission submission = objectMapper.readValue(requestBody, Submission.class);
			String waitingroom = submissionService.populateSyncFields(submission);
			priorityQueueService.enqueue(submission);

			int timeout = submission.getDockerTimeout() == null ? 120 : submission.getDockerTimeout();
			while (!syncWaitingRoom.containsKey(waitingroom) && timeout > 0) {
				TimeUnit.SECONDS.sleep(1);
				timeout--;
			}
			return syncWaitingRoom.remove(waitingroom);

		} catch (JsonProcessingException | InterruptedException e) {
			LOGGER.error("Request format invalid: {}", e.getMessage());
			throw new RequestFormatException(e.getMessage());
		}
	}

	public void waitingroom(HttpEntity<String> httpEntity, String hash) {
		try {
			syncWaitingRoom.put(hash, objectMapper.readValue(Objects.requireNonNull(httpEntity.getBody()), AreteResponse.class));
		} catch (Exception e) {
			LOGGER.error("Processing sync job failed: {}", e.getMessage());
			syncWaitingRoom.put(hash, new AreteResponse("NaN", new Submission(), e.getMessage()));
		}
	}

	public String updateImage(String image) {
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

	public String updateTests(HttpEntity<String> httpEntity) {
		try {
			String requestBody = httpEntity.getBody();
			LOGGER.info("Parsing request body: " + requestBody);
			if (requestBody == null) throw new RequestFormatException("Empty input!");
			AreteTestUpdate update = objectMapper.readValue(requestBody, AreteTestUpdate.class);

			assert update.getProject().getPath_with_namespace() != null;
			assert update.getProject().getUrl() != null;
			update.getProject().setUrl(submissionService.fixRepository(update.getProject().getUrl()));

			String pathToTesterFolder = String.format("tests/%s/", update.getProject().getPath_with_namespace());
			String pathToTesterRepo = update.getProject().getUrl();

			priorityQueueService.halt();
			gitPullService.pullOrClone(pathToTesterFolder, pathToTesterRepo, Optional.empty());
			priorityQueueService.go();

			try {
				// test the solution on some repository
				DefaultParameters params = objectMapper.readValue(new File(String.format("tests/%s/arete.json", update.getProject().getPath_with_namespace())), DefaultParameters.class);
				Submission submission = new Submission();
				AreteTestUpdate.Commit latest = update.getCommits().get(0);
				submission.setTestingPlatform(params.getProgrammingLanguage());
				submission.setEmail(latest.getAuthor().getEmail());
				submission.setUniid(update.getProject().getNamespace());
				submission.setGitStudentRepo(params.getSolutionsRepository());
				submission.setGitTestSource(update.getProject().getUrl());
				Set<String> slugs = new HashSet<>();
				slugs.addAll(latest.getAdded());
				slugs.addAll(latest.getModified());
				submission.setSlugs(slugs);
				submissionService.populateAsyncFields(submission);
				submission.setCourse(update.getProject().getPath_with_namespace());
				LOGGER.info("Initial slugs: {}", slugs);
				jobRunnerService.formatSlugs(submission);
				LOGGER.info("Final submission: {}", submission);
				priorityQueueService.enqueue(submission);
			} catch (Exception ignored) {
				// no testing
			}

			return "Successfully updated tests: " + update.getProject().getPath_with_namespace();
		} catch (Exception e) {
			throw new RequestFormatException(e.getMessage());
		}
	}
}
