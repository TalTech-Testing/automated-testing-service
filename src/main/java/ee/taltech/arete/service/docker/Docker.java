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
import ee.taltech.arete.api.data.SourceFile;
import ee.taltech.arete.domain.InputWriter;
import ee.taltech.arete.domain.Submission;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import static com.github.dockerjava.api.model.AccessMode.ro;
import static com.github.dockerjava.api.model.AccessMode.rw;
import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

public class Docker {

	private static final String home = System.getenv().getOrDefault("ARETE_HOME", System.getenv("HOME") + "/arete");
	private static Logger LOGGER = LoggerFactory.getLogger(Docker.class);
	public String hostFile;
	private ObjectMapper mapper = new ObjectMapper();

	private DockerClient dockerClient;
	private CreateContainerResponse container;
	private String imageId;
	private String containerName;
	private String image;

	private Submission submission;
	private String slug;

	public Docker(Submission submission, String slug) {
		this.submission = submission;
		this.slug = slug;

		this.containerName = String.format("%s_%s_%s", submission.getHash().substring(0, 16).toLowerCase(), submission.getThread(), 100000 + Math.abs(new Random().nextInt()) * 900000);
		this.hostFile = String.format("input_and_output/%s/host/output.json", submission.getThread());
		TestingPlatforms testingPlatforms = TestingPlatforms.BY_LABEL.get(submission.getTestingPlatform());
		TestingPlatforms.correctTesterInput(submission);
		this.image = testingPlatforms.image;
	}

	public void run() {
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

			///  PROCEED TO MODIFY WITH CAUTION  ///

			String output = String.format("%s/input_and_output/%s/host", home, submission.getThread());
			String testerHost = String.format("%s/input_and_output/%s/tester", home, submission.getThread());
			String studentHost = String.format("%s/input_and_output/%s/student", home, submission.getThread());

			String tester = String.format("tests/%s/%s", submission.getProject(), slug);
			String tempTester = String.format("input_and_output/%s/tester", submission.getThread());

			String student = String.format("students/%s/%s/%s", submission.getUniid(), submission.getProject(), slug);
			String tempStudent = String.format("input_and_output/%s/student", submission.getThread());

			Volume volumeStudent = new Volume("/student");
			Volume volumeTester = new Volume("/tester");
			Volume volumeOutput = new Volume("/host");

			try {
				if (submission.getSource() == null) {
					FileUtils.copyDirectory(new File(student), new File(tempStudent));
				} else {
					for (SourceFile file : submission.getSource()) {
						File path = new File(String.format("%s/%s", tempStudent, file.getPath().substring(file.getPath().indexOf("\\"))));
						path.getParentFile().mkdirs();
						FileWriter writer = new FileWriter(path);
						writer.write(file.getContents());
						writer.close();
					}
				}
			} catch (IOException e) {
				LOGGER.error("Failed to copy files from student folder to temp folder.");
				throw new IOException(e.getMessage());
			}

			try {
				FileUtils.copyDirectory(new File(tester), new File(tempTester));
			} catch (IOException e) {
				LOGGER.error("Failed to copy files from tester folder to temp folder.");
				throw new IOException(e.getMessage());
			}

			mapper.writeValue(new File(String.format("input_and_output/%s/host/input.json", submission.getThread())), new InputWriter(String.join(",", submission.getDockerExtra())));

			container = dockerClient.createContainerCmd(imageId)
					.withName(containerName)
					.withVolumes(volumeStudent, volumeTester, volumeOutput)
					.withAttachStdout(true)
					.withAttachStderr(true)
					.withHostConfig(newHostConfig()
							.withBinds(
									new Bind(new File(output).getAbsolutePath(), volumeOutput, rw),
									new Bind(new File(studentHost).getAbsolutePath(), volumeStudent, rw),
									new Bind(new File(testerHost).getAbsolutePath(), volumeTester, ro)))
					.exec();

			///   END OF WARNING   ///

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
							submission.getResult().append(new String(frame.getPayload()));
//							System.out.print(new String(frame.getPayload()));
						}
					});

			LOGGER.info("Docker for user {} with slug {} finished", submission.getUniid(), slug);

		} catch (Exception e) {
			LOGGER.error("Job failed with exception: {}", e.getMessage());
			throw new DockerException("Cant't launch docker, message: " + e.getMessage(), 1);
		}
	}

	public void cleanup() {
		if (dockerClient != null && container != null) {

			try {
				dockerClient.stopContainerCmd(container.getId()).withTimeout(200).exec();
				LOGGER.info("Stopped container: {}", container.getId());
			} catch (Exception stop) {
				LOGGER.info("Container {} has already been stopped", container.getId());
			}

			try {
				dockerClient.removeContainerCmd(container.getId()).exec();
				LOGGER.info("Removed container: {}", container.getId());
			} catch (Exception remove) {
				LOGGER.error("Container {} has already been removed", submission.getHash());
			}
		}

		try {
			String tempTester = String.format("input_and_output/%s/tester", submission.getThread());
			FileUtils.cleanDirectory(new File(tempTester));
		} catch (IOException e) {
			LOGGER.error("Temp folder already empty. {}", e.getMessage());
		}

		try {
			String tempStudent = String.format("input_and_output/%s/student", submission.getThread());
			FileUtils.cleanDirectory(new File(tempStudent));
		} catch (IOException e) {
			LOGGER.error("Temp folder already empty. {}", e.getMessage());
		}
	}

	private String getImage(DockerClient dockerClient, String image) throws InterruptedException {

		ImageCheck imageCheck = new ImageCheck(dockerClient, image);
		imageCheck.invoke();
		return imageCheck.getTester().getId();

	}

}
