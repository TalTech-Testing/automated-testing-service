package ee.taltech.arete.component;

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
