package ee.taltech.arete_testing_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import ee.taltech.arete.java.request.hook.AreteTestUpdateDTO;
import ee.taltech.arete.java.request.hook.CommitDTO;
import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete_testing_service.domain.OverrideParameters;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.exception.RequestFormatException;
import ee.taltech.arete_testing_service.service.arete.AreteConstructor;
import ee.taltech.arete_testing_service.service.docker.ImageCheck;
import ee.taltech.arete_testing_service.service.git.GitPullService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RequestService {

	private final Logger logger;
	private final ObjectMapper objectMapper;
	private final SubmissionService submissionService;
	private final SubmissionPropertyService submissionPropertyService;
	private final GitPullService gitPullService;
	private final PriorityQueueService priorityQueueService;
	private final HashMap<String, AreteResponseDTO> syncWaitingRoom = new HashMap<>();

	@SneakyThrows
	public Submission testAsync(HttpEntity<String> request) {
		Submission submission = objectMapper.readValue(request.getBody(), Submission.class);
		submissionService.populateAsyncFields(submission);
		priorityQueueService.enqueue(submission);
		return submission;
	}

	@SneakyThrows
	public AreteResponseDTO testSync(HttpEntity<String> request) {
		Submission submission = objectMapper.readValue(request.getBody(), Submission.class);
		String waitingroom = submissionService.populateSyncFields(submission);
		priorityQueueService.enqueue(submission);
		int timeout = submission.getDockerTimeout() == null ? 120 : submission.getDockerTimeout();
		while (!syncWaitingRoom.containsKey(waitingroom) && timeout > 0) {
			TimeUnit.SECONDS.sleep(1);
			timeout--;
		}

		return syncWaitingRoom.remove(waitingroom);

	}

	public void waitingroom(HttpEntity<String> httpEntity, String hash) {
		try {
			syncWaitingRoom.put(hash, objectMapper.readValue(Objects.requireNonNull(httpEntity.getBody()), AreteResponseDTO.class));
		} catch (Exception e) {
			logger.error("Processing sync job failed: {}", e.getMessage());
			syncWaitingRoom.put(hash, AreteConstructor.failedSubmission("NaN", new Submission(), e.getMessage()));
		}
	}

	public String updateImage(String image) {
		try {
			PriorityQueueService.halt();
			String dockerHost = System.getenv().getOrDefault("DOCKER_HOST", "unix:///var/run/docker.sock");
			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost(dockerHost)
					.withDockerTlsVerify(false)
					.build();
			new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/" + image).pull();
			PriorityQueueService.go();
			return "Successfully updated image: " + image;
		} catch (Exception e) {
			throw new RequestFormatException(e.getMessage());
		}
	}

	public String updateTests(HttpEntity<String> httpEntity) {
		String requestBody = httpEntity.getBody();
		logger.info("Parsing request body: " + requestBody);

		AreteTestUpdateDTO update = mapUpdateRequest(requestBody);
		update.getProject().setUrl(submissionService.fixRepository(update.getProject().getUrl()));
		String pathToTesterFolder = String.format("tests/%s/", update.getProject().getPath_with_namespace());
		String pathToTesterRepo = update.getProject().getUrl();

		try {
			PriorityQueueService.halt();
			gitPullService.pullOrClone(pathToTesterFolder, pathToTesterRepo, Optional.empty());
			PriorityQueueService.go();
		} catch (Exception e) {
			throw new RequestFormatException(e.getMessage());
		}

		try {
			runVerifyingTests(update);
		} catch (Exception ignored) {
			// no testing
		}

		return "Successfully updated tests: " + update.getProject().getPath_with_namespace();
	}

	private AreteTestUpdateDTO mapUpdateRequest(String requestBody) {
		if (requestBody == null) throw new RequestFormatException("Empty input!");
		AreteTestUpdateDTO update;
		try {
			update = objectMapper.readValue(requestBody, AreteTestUpdateDTO.class);
			assert update.getProject().getPath_with_namespace() != null;
			assert update.getProject().getUrl() != null;
		} catch (Exception e) {
			throw new RequestFormatException(e.getMessage());
		}
		return update;
	}

	public void testingProperties(Submission submission) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				OverrideParameters params = objectMapper.readValue(new File(String.format("tests/%s/arete.json", submission.getCourse())), OverrideParameters.class);
				params.overrideParametersForTestValidation(submission);
				logger.info("Overrode default parameters: {}", params);
			} catch (Exception e) {
				logger.info("Using default parameters: {}", e.getMessage());
			}
		}
	}

	private void runVerifyingTests(AreteTestUpdateDTO update) {
		Submission submission = new Submission();
		CommitDTO latest = update.getCommits().get(0);
		submission.setEmail(latest.getAuthor().getEmail());
		submission.setUniid(update.getProject().getNamespace());
		submission.setGitTestRepo(update.getProject().getUrl());
		this.testingProperties(submission);
		Set<String> slugs = new HashSet<>();
		slugs.addAll(latest.getAdded());
		slugs.addAll(latest.getModified());
		submission.setSlugs(slugs);
		submissionService.populateAsyncFields(submission);
		submission.setCourse(update.getProject().getPath_with_namespace());
		logger.info("Initial slugs: {}", slugs);
		submissionPropertyService.formatSlugs(submission);
		logger.info("Final submission: {}", submission);
		priorityQueueService.enqueue(submission);
	}

	public List<Submission> getActiveSubmissions() {
		return priorityQueueService.getActiveSubmissions();
	}
}
