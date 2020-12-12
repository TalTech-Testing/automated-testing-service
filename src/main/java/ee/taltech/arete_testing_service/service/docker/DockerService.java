package ee.taltech.arete_testing_service.service.docker;

import ee.taltech.arete_testing_service.domain.Submission;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DockerService {

	private final Logger logger;

	/**
	 * @param submission : test job to be tested.
	 * @return test job result path
	 */
	public String runDocker(Submission submission, String slug) throws Exception {

		DockerTestRunner docker = new DockerTestRunner(submission, slug);
		Exception exception = null;

		try {

			docker.run();

		} catch (Exception e) {
			logger.error("Failed running docker: {}", e.getMessage());
			exception = e;

		} finally {
			docker.cleanup();
		}

		if (exception == null) {
			return docker.outputPath;
		} else {
			throw exception;
		}

	}

}
