package ee.taltech.arete.service.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.DockerClient;
import ee.taltech.arete.domain.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.concurrent.TimeUnit.SECONDS;

@Service
public class JobRunnerServiceImpl implements JobRunnerService {

    @Value("${testing.docker-log-pattern}")
    String DOCKER_LOG_MAPPING_PATTERN;

    @Value("${testing.docker-timeout}")
    int DOCKER_TIMEOUT;

    @Value("${testing.docker-mapping-pattern}")
    String DOCKER_MAPPING_PATTERN;

    @Value("${testing.tester-prefix}")
    String TESTER_PREFIX;

    @Value("${testing.folder-prefix}")
    public String DEFAULT_PREFIX;

    @Value("${testing.shared-folder}")
    public String SHARED_FOLDER_LOCATION;

    private static final String DOCKER_URI = "unix:///var/run/docker.sock";
    private static final long DOCKER_CONN_TIMEOUT = SECONDS.toMillis(6L);

    private static Logger LOGGER = LoggerFactory.getLogger(JobRunnerServiceImpl.class);


    @Override
    public void runJob(Submission submission) {
        System.out.println("RUNNING JOB " + submission);
        pullJobRequirements(submission);
    }

    private void pullJobRequirements(Submission submission) {
//        for () {
//            submission.setSlug("TODO");
//            runDocker(submission);
//        }
    }

    private void runDocker(Submission testJob) {



    }
}


// java -jar tester-java/build/libs/studenttester-core-2.0.jar -c envomp/iti0202-2019/EX01IdCode/ -t iti0202-2019/ex/EX01IdCode/ -jsontxt -r REPORT,CHECKSTYLE,JAR,COMPILER,TESTNG
