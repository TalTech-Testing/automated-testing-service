package ee.taltech.arete_testing_service.component;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import ee.taltech.arete_testing_service.service.PriorityQueueService;
import ee.taltech.arete_testing_service.service.docker.ImageCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ApplicationStartup implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);

	private final PriorityQueueService priorityQueueService;

	@Autowired
	public ApplicationStartup(PriorityQueueService priorityQueueService) {
		this.priorityQueueService = priorityQueueService;
	}

	@Override
	public void run(ApplicationArguments applicationArguments) {
		log.info("setting up temp folders.");

		createDirectory("input_and_output");
		createDirectory("students");
		createDirectory("tests");

		try {
			String dockerHost = System.getenv().getOrDefault("DOCKER_HOST", "unix:///var/run/docker.sock");

			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost(dockerHost)
					.withDockerTlsVerify(false)
					.build();

			new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/java-tester").pull();
			new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/python-tester").pull();
			new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/prolog-tester").pull();
			new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/fsharp-tester").pull();
			new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/uva-tester").pull();
		} catch (Exception ignored) {
		}

		log.info("Done setup");
		priorityQueueService.go();

	}

	private void createDirectory(String home) {
		File file = new File(home);
		if (!file.exists()) {
			if (!file.exists()) {
				file.mkdir();
			}
		}
	}

}
