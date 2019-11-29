package ee.taltech.arete.component;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import ee.taltech.arete.service.docker.ImageCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ApplicationStartup implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);

	@Override
	public void run(ApplicationArguments applicationArguments) throws Exception {
		log.info("setting up temp folders.");

		String home = System.getenv().getOrDefault("ARETE_HOME", System.getenv("HOME") + "/arete");

		createDirectory(String.format("%s/input_and_output", home));
		createDirectory(String.format("%s/students", home));
		createDirectory(String.format("%s/tests", home));

		for (int i = 0; i < 16; i++) {

			createDirectory(String.format("%s/input_and_output/%s", home, i));
			createDirectory(String.format("%s/input_and_output/%s/tester", home, i));
			createDirectory(String.format("%s/input_and_output/%s/host", home, i));

			try {
				new File(String.format("%s/input_and_output/%s/host/input.json", home, i)).createNewFile();
			} catch (Exception ignored) {
			}

			try {
				new File(String.format("%s/input_and_output/%s/host/output.json", home, i)).createNewFile();
			} catch (Exception ignored) {
			}

		}

		String dockerHost = System.getenv().getOrDefault("DOCKER_HOST", "unix:///var/run/docker.sock");

		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost(dockerHost)
				.withDockerTlsVerify(false)
				.build();

		new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/java-tester:latest").pull();
		new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/python-tester:latest").pull();

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
