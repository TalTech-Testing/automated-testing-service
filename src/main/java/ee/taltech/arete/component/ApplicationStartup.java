package ee.taltech.arete.component;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import ee.taltech.arete.service.docker.ImageCheck;
import ee.taltech.arete.service.git.GitPullService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class ApplicationStartup implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);

	@Autowired
	private GitPullService gitPullService;

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
			createDirectory(String.format("%s/input_and_output/%s/student", home, i));
			createDirectory(String.format("%s/input_and_output/%s/host", home, i));

			try {
				new File(String.format("%s/input_and_output/%s/host/input.json", home, i)).createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				new File(String.format("%s/input_and_output/%s/host/output.json", home, i)).createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		try {
			String dockerHost = System.getenv().getOrDefault("DOCKER_HOST", "unix:///var/run/docker.sock");

			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost(dockerHost)
					.withDockerTlsVerify(false)
					.build();

			new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/java-tester:latest").pull();
			new ImageCheck(DockerClientBuilder.getInstance(config).build(), "automatedtestingservice/python-tester:latest").pull();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			List<String> projects = Arrays.asList("iti0102-2019", "iti0202-2019");
			List<String> projectBases = Arrays.asList("ex", "ex");

			for (int i = 0; i < projectBases.size(); i++) {
				String pathToTesterFolder = String.format("tests/%s/", projects.get(i));
				String pathToTesterRepo = String.format("https://gitlab.cs.ttu.ee/%s/%s.git", projects.get(i), projectBases.get(i));
				gitPullService.pullOrClone(pathToTesterFolder, pathToTesterRepo, Optional.empty());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

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
