package ee.taltech.arete.service.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.domain.TestUpdate;
import ee.taltech.arete.exception.RequestFormatException;
import ee.taltech.arete.service.docker.ImageCheck;
import ee.taltech.arete.service.git.GitPullService;
import ee.taltech.arete.service.queue.PriorityQueueService;
import ee.taltech.arete.service.submission.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class requestServiceImpl implements RequestService {

    private static Logger LOGGER = LoggerFactory.getLogger(RequestService.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private PriorityQueueService priorityQueueService;

    @Autowired
    private GitPullService gitPullService;

    private HashMap<String, AreteResponse> syncWaitingRoom = new HashMap<>();

    @Override
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

    @Override
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

    @Override
    public void waitingroom(HttpEntity<String> httpEntity, String hash) {
        try {
            syncWaitingRoom.put(hash, objectMapper.readValue(Objects.requireNonNull(httpEntity.getBody()), AreteResponse.class));
        } catch (Exception e) {
            LOGGER.error("Processing sync job failed: {}", e.getMessage());
            syncWaitingRoom.put(hash, new AreteResponse("NaN", new Submission(), e.getMessage()));
        }
    }

    @Override
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

    @Override
    public String updateTests(HttpEntity<String> httpEntity) {
        try {
            String requestBody = httpEntity.getBody();
            LOGGER.info("Parsing request body: " + requestBody);
            if (requestBody == null) throw new RequestFormatException("Empty input!");
            TestUpdate update = objectMapper.readValue(requestBody, TestUpdate.class);

            assert update.getProject().getPath_with_namespace() != null;
            assert update.getProject().getUrl() != null;
            update.getProject().setUrl(submissionService.fixRepository(update.getProject().getUrl()));

            String pathToTesterFolder = String.format("tests/%s/", update.getProject().getPath_with_namespace());
            String pathToTesterRepo = update.getProject().getUrl();

            priorityQueueService.halt();
            LOGGER.info("Checking for update for tester:");
            gitPullService.pullOrClone(pathToTesterFolder, pathToTesterRepo, Optional.empty());
            priorityQueueService.go();

            return "Successfully updated tests: " + update.getProject().getPath_with_namespace();
        } catch (Exception e) {
            throw new RequestFormatException(e.getMessage());
        }
    }
}
