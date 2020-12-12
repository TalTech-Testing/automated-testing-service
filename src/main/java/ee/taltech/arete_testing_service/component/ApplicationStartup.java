package ee.taltech.arete_testing_service.component;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import ee.taltech.arete_testing_service.service.PriorityQueueService;
import ee.taltech.arete_testing_service.service.docker.ImageCheck;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@AllArgsConstructor
public class ApplicationStartup implements ApplicationRunner {

	private final Logger logger;

	@Override
	public void run(ApplicationArguments applicationArguments) {
		logger.info("setting up temp folders.");

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
			new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/hackerrank-tester").pull();
		} catch (Exception ignored) {
		}

		logger.info("Done setup");
		PriorityQueueService.go();

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
