package ee.taltech.arete.service.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import ee.taltech.arete.domain.InputWriter;
import ee.taltech.arete.domain.Submission;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.github.dockerjava.api.model.AccessMode.ro;
import static com.github.dockerjava.api.model.AccessMode.rw;
import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

@Service
public class DockerServiceImpl implements DockerService {

	private static Logger LOGGER = LoggerFactory.getLogger(DockerService.class);

	private static final String home = System.getenv().getOrDefault("ARETE_HOME", System.getenv("HOME") + "/arete");

	private ObjectMapper mapper = new ObjectMapper();

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

	/**
	 * @param submission : test job to be tested.
	 * @return test job result path
	 */
	public String runDocker(Submission submission, String slug) {

		DockerClient dockerClient = null;
		CreateContainerResponse container = null;
		String imageId;

		String containerName = String.format("%s_%s", submission.getHash().substring(0, 8).toLowerCase(), submission.getThread());
		String hostFile = String.format("input_and_output/%s/host/output.json", submission.getThread());
		TestingPlatforms testingPlatforms = TestingPlatforms.BY_LABEL.get(submission.getTestingPlatform());
		TestingPlatforms.correctTesterInput(submission);
		String image = testingPlatforms.image;

		try {

			String dockerHost = System.getenv().getOrDefault("DOCKER_HOST", "unix:///var/run/docker.sock");
//			String certPath = System.getenv().getOrDefault("DOCKER_CERT_PATH", "/home/user/.docker/certs");
//			String tlsVerify = System.getenv().getOrDefault("DOCKER_TLS_VERIFY", "1");
//			String dockerConfig = System.getenv().getOrDefault("DOCKER_CONFIG", "/home/user/.docker");

			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost(dockerHost)
					.withDockerTlsVerify(false)
					.build();

			dockerClient = DockerClientBuilder.getInstance(config).build();

			imageId = getImage(dockerClient, image);

			LOGGER.info("Got image with id: {}", imageId);

			String student = String.format("%s/students/%s/%s/%s", home, submission.getUniid(), submission.getProject(), slug);
			String tester = String.format("%s/tests/%s/%s", home, submission.getProject(), slug);
			String tempTester = String.format("%s/input_and_output/%s/tester", home, submission.getThread()); // Slug into temp folder

			try {
				FileUtils.copyDirectory(new File(tester), new File(tempTester));
			} catch (IOException e) {
				LOGGER.error("Failed to copy files from tester folder to temp folder.");
				throw new IOException(e.getMessage());
			}

			String output = String.format("%s/input_and_output/%s/host", home, submission.getThread());

			Volume volumeStudent = new Volume("/student");
			Volume volumeTester = new Volume("/tester");
			Volume volumeOutput = new Volume("/host");

			mapper.writeValue(new File(String.format("%s/input_and_output/%s/host/input.json", home, submission.getThread())), new InputWriter(String.join(",", submission.getExtra())));

			container = dockerClient.createContainerCmd(imageId)
					.withName(containerName)
					.withVolumes(volumeStudent, volumeTester, volumeOutput)
					.withAttachStdout(true)
					.withAttachStderr(true)
					.withHostConfig(newHostConfig()
							.withBinds(
									new Bind(output, volumeOutput, rw),
									new Bind(student, volumeStudent, rw),
									new Bind(tempTester, volumeTester, ro)))
					.exec();

			LOGGER.info("Created container with id: {}", container.getId());

			dockerClient.startContainerCmd(container.getId()).exec();
			LOGGER.info("Started container with id: {}", container.getId());

//			dockerClient.waitContainerCmd(container.getId())
//					.exec(new WaitContainerResultCallback());

			dockerClient
					.logContainerCmd(containerName)
					.withStdErr(true)
					.withStdOut(true)
					.withFollowStream(true)
					.withSince(0)
					.exec(new ResultCallbackTemplate<LogContainerResultCallback, Frame>() {
						@Override
						public void onNext(Frame frame) {
							System.out.print(new String(frame.getPayload()));
						}
					});
			LOGGER.info("Docker finished with status code: ");

		} catch (Exception e) {
			LOGGER.error("Job failed with exception: {}", e.getMessage());
			cleanup(submission, dockerClient, container);
			throw new DockerException("Cant't launch docker, message: " + e.getMessage(), 1);
		}

		cleanup(submission, dockerClient, container);

		return hostFile;
	}

	private void cleanup(Submission submission, DockerClient dockerClient, CreateContainerResponse container) {
		if (dockerClient != null && container != null) {

			LOGGER.info("Stopping container: {}", container.getId());
			try {
				dockerClient.stopContainerCmd(container.getId()).withTimeout(200).exec();
			} catch (Exception stop) {
				LOGGER.info("Container {} has already been stopped", container.getId());
			}

			LOGGER.info("Removing container: {}", container.getId());
			try {
				dockerClient.removeContainerCmd(container.getId()).exec();
			} catch (Exception remove) {
				LOGGER.error("Container {} has already been removed", submission.getHash());
			}
		}

		try {
			String tempTester = String.format("%s/input_and_output/%s/tester", home, submission.getThread());
			FileUtils.cleanDirectory(new File(tempTester));
		} catch (IOException e) {
			LOGGER.error("Temp folder already empty.");
		}
	}

	private String getImage(DockerClient dockerClient, String image) {

		ImageCheck imageCheck = new ImageCheck(dockerClient, image);
		imageCheck.invoke();
		return imageCheck.getTester().getId();

	}

}
