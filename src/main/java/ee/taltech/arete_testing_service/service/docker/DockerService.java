package ee.taltech.arete_testing_service.service.docker;

import ee.taltech.arete_testing_service.domain.Submission;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class DockerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DockerService.class);

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
			LOGGER.error("Failed running docker: {}", e.getMessage());
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
