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

    	//TODO dis
    	//docker build --build-arg uniid=envomp --build-arg slug=EX01IdCode -f Dockerfile-java-test-job -t somehash . && docker run -it somehash

    }
}


// java -jar tester-java/build/libs/studenttester-core-2.0.jar -c envomp/iti0202-2019/EX01IdCode/ -t iti0202-2019/ex/EX01IdCode/ -jsontxt -r REPORT,CHECKSTYLE,JAR,COMPILER,TESTNG

// docker build --build-arg uniid=envomp --build-arg slug=EX01IdCode -f Dockerfile-java-test-job -t somehash . && docker run -it somehash