package ee.taltech.arete.service.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import ee.taltech.arete.domain.Submission;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class DockerServiceImpl implements DockerService {

	private static Logger LOGGER = LoggerFactory.getLogger(DockerService.class);


	/**
	 * @param submission : test job to be tested.
	 * @return test job result path
	 */
	public String runDocker(Submission submission, String slug) {

		DockerClient dockerClient = null;
		CreateContainerResponse container = null;
		String imageId = null;

		String containerName = submission.getHash().substring(0, 16).toLowerCase();
		String containerFile = "/output/output.json";
		String hostFile = String.format("output/%s_%s_%s_%s.json", submission.getUniid().toLowerCase(), submission.getProject(), slug.toLowerCase(), containerName);

		String dockerfile = TestingPlatforms.BY_LABEL.get(submission.getTestingPlatform()).dockerfileLocation;

		try {

			String dockerHost = System.getenv().getOrDefault("DOCKER_HOST", "unix:///var/run/docker.sock");

			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost(dockerHost)
					.withDockerTlsVerify(false)
					.build();

			dockerClient = DockerClientBuilder.getInstance(config).build();

			imageId =
					dockerClient.buildImageCmd()
							.withBuildArg("uniid", submission.getUniid())
							.withBuildArg("slug", slug)
							.withBuildArg("project", submission.getProject())
							.withBaseDirectory(new File("./"))
							.withDockerfile(new File(dockerfile))
							.withPull(true)
							.withTag(containerName)
							.withNoCache(false)
							.exec(new BuildImageResultCallback())
							.awaitImageId();

			LOGGER.info("Created image with id: {}", imageId);

			container = dockerClient.createContainerCmd(containerName + ":latest").withName(containerName).exec();
			LOGGER.info("Created container with id: {}", container.getId());

			dockerClient.startContainerCmd(container.getId()).exec();
			LOGGER.info("Started container with id: {}", container.getId());

			Integer statusCode = dockerClient.waitContainerCmd(container.getId())
					.exec(new WaitContainerResultCallback())
					.awaitStatusCode();
			LOGGER.info("Docker finished with status code: {}", statusCode);

			try {
				InputStream inputStream = dockerClient.copyArchiveFromContainerCmd(containerName, containerFile).exec();
				TarArchiveInputStream tarStream = new TarArchiveInputStream(inputStream);
				unTar(tarStream, new File(hostFile));

			} catch (Exception tar) {
				tar.printStackTrace();
			}
			LOGGER.info("Copying output from container: {}", container.getId());


		} catch (Exception e) {

			e.printStackTrace();
			LOGGER.error("Job failed with exception: {}", e.getMessage());
		}

		if (dockerClient != null && container != null) {

			LOGGER.info("Stopping container: {}", container.getId());
			try {
				dockerClient.stopContainerCmd(container.getId()).withTimeout(20).exec();
			} catch (Exception stop) {
				LOGGER.info("Container {} has already been stopped", container.getId());
			}

			LOGGER.info("Removing container: {}", container.getId());
			try {
				dockerClient.removeContainerCmd(container.getId()).exec();
			} catch (Exception remove) {
				LOGGER.error("Container {} has already been removed", submission.getHash());
			}

			LOGGER.info("Removing image: {}", imageId);
			try {
				dockerClient.removeImageCmd(imageId).exec();
			} catch (Exception image) {
				LOGGER.error("Image {} has already been removed", imageId);
			}

		}


		return hostFile;
	}

	private static void unTar(TarArchiveInputStream tis, File destFile) throws IOException {
		TarArchiveEntry tarEntry = null;
		while ((tarEntry = tis.getNextTarEntry()) != null) {
			if (tarEntry.isDirectory()) {
				if (!destFile.exists()) {
					boolean a = destFile.mkdirs();
				}
			} else {
				FileOutputStream fos = new FileOutputStream(destFile);
				IOUtils.copy(tis, fos);
				fos.close();
			}
		}
		tis.close();
	}
}
