package ee.taltech.arete_testing_service.service.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.RestartPolicy;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.jaxrs.JerseyDockerHttpClient;
import ee.taltech.arete.java.request.tester.DockerParameters;
import ee.taltech.arete.java.response.arete.FileDTO;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.exception.DockerRunnerException;
import ee.taltech.arete_testing_service.exception.DockerTimeoutException;
import ee.taltech.arete_testing_service.exception.ImageNotFoundException;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dockerjava.api.model.AccessMode.ro;
import static com.github.dockerjava.api.model.AccessMode.rw;
import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

public class DockerTestRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(DockerTestRunner.class);
	public static final long HIGH_PRIORITY_CPUS = 4L;
	public static final long LOW_PRIORITY_CPUS = 2L;
	public static final long HIGH_PRIORITY_MEMORY = 8000000000L;
	public static final long LOW_PRIORITY_MEMORY = 4000000000L;
	private final ObjectMapper mapper = new ObjectMapper();

	private final String containerName;
	private final String image;
	private final Submission submission;
	private final String slug;
	public String outputPath;
	private CreateContainerResponse container;
	private String containerId;
	private DockerClient dockerClient;
	private boolean done = false;
	private final List<Volume> volumes = new ArrayList<>();

	public DockerTestRunner(Submission submission, String slug) {
		this.submission = submission;
		this.slug = slug;
		this.containerName = String.format("%s_%s", submission.getHash().substring(0, 16).toLowerCase(), 100000 + Math.abs(new Random().nextInt()) * 900000);
		this.outputPath = String.format("input_and_output/%s/%s/host", submission.getHash(), slug);
		this.image = String.format("automatedtestingservice/%s-tester", submission.getTestingPlatform());
	}

	public String run() {
		try {
			String dockerHost = System.getenv().getOrDefault("DOCKER_HOST", "unix:///var/run/docker.sock");

			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost(dockerHost)
					.withDockerTlsVerify(false)
					.build();

			dockerClient = DockerClientBuilder
					.getInstance(config)
					.withDockerHttpClient(
							new JerseyDockerHttpClient.Builder()
									.dockerHost(new URI(dockerHost))
									.sslConfig(config.getSSLConfig())
									.build())
					.build();

			String imageId = getImage(dockerClient, image);

			LOGGER.info("Got image with id: {}", imageId);

			String output = String.format("input_and_output/%s/%s/host", submission.getHash(), slug);
			String testerHost = String.format("input_and_output/%s/%s/tester", submission.getHash(), slug);
			String studentHost = String.format("input_and_output/%s/%s/student", submission.getHash(), slug);

			String tester = String.format("tests/%s/%s", submission.getCourse(), slug);
			String tempTester = String.format("input_and_output/%s/%s/tester", submission.getHash(), slug);

			String student = String.format("students/%s/%s/%s", submission.getUniid(), submission.getFolder(), slug);
			String tempStudent = String.format("input_and_output/%s/%s/student", submission.getHash(), slug);

			Volume volumeStudent = new Volume("/student");
			Volume volumeTester = new Volume("/tester");
			Volume volumeOutput = new Volume("/host");
			volumes.add(volumeStudent);
			volumes.add(volumeTester);
			volumes.add(volumeOutput);

			if (!submission.getSystemExtra().contains("skipCopying") && !submission.getSystemExtra().contains("skipCopyingStudent")) {
				copyFiles(student, tempStudent, submission.getFolder(), submission.getSource(), "Failed to copy files from student folder to temp folder.");
			}
			if (!submission.getSystemExtra().contains("skipCopying") && !submission.getSystemExtra().contains("skipCopyingTests")) {
				copyFiles(tester, tempTester, submission.getCourse(), submission.getTestSource(), "Failed to copy files from tester folder to temp folder.");

				for (String testerFolder : submission.getTesterFolders()) {
					String testerFolderPath = String.format("tests/%s/%s", submission.getCourse(), testerFolder);
					String tempTesterFolderPath = String.format("input_and_output/%s/%s/tester/%s", submission.getHash(), slug, testerFolder);
					copyFiles(testerFolderPath, tempTesterFolderPath, submission.getCourse(), submission.getTestSource(),
							"Failed to copy files from " + testerFolder + " folder to tester/" + testerFolder + " folder.");
				}
			}

			mapper.writeValue(new java.io.File(String.format("input_and_output/%s/%s/host/input.json", submission.getHash(), slug)),
					DockerParameters.builder()
							.contentRoot(submission.getDockerContentRoot())
							.testRoot(submission.getDockerTestRoot())
							.extra(submission.getDockerExtra())
							.commitMessage(submission.getCommitMessage())
							.systemExtra(submission.getSystemExtra())
							.timeout(submission.getDockerTimeout())
							.uniid(submission.getUniid()).build());

			final String uniidEnv = "uniid=" + submission.getUniid();
			final String hashEnv = "submission_hash=" + submission.getHash();
			final String systemExtraEnv = "system_extra=" + String.join(", ", submission.getSystemExtra().toArray(new String[0]));
			final String dockerExtraEnv = "docker_extra=" + submission.getDockerExtra();
			final String dockerTimeoutEnv = "docker_timeout=" + submission.getDockerTimeout();
			final String dockerContentRootEnv = "docker_content_root=" + submission.getDockerContentRoot();
			final String dockerTestRootEnv = "docker_test_root=" + submission.getDockerTestRoot();
			final String commitMessageEnv = "commit_message=" + submission.getCommitMessage();

			container = dockerClient.createContainerCmd(imageId)
					.withName(containerName)
					.withEnv(uniidEnv, hashEnv, systemExtraEnv, dockerExtraEnv, dockerTimeoutEnv,
							dockerContentRootEnv, dockerTestRootEnv, commitMessageEnv)
					.withVolumes(volumeStudent, volumeTester, volumeOutput)
					.withAttachStdout(true)
					.withAttachStderr(true)
					.withHostConfig(newHostConfig()
							.withBinds(
									new Bind(new java.io.File(output).getAbsolutePath(), volumeOutput, rw),
									new Bind(new java.io.File(studentHost).getAbsolutePath(), volumeStudent, rw),
									new Bind(new java.io.File(testerHost).getAbsolutePath(), volumeTester, ro))
							.withCpuCount((isPriorityJob(submission) ? HIGH_PRIORITY_CPUS : LOW_PRIORITY_CPUS))
							.withMemory((isPriorityJob(submission) ? HIGH_PRIORITY_MEMORY : LOW_PRIORITY_MEMORY))
							.withMemorySwap((isPriorityJob(submission) ? HIGH_PRIORITY_MEMORY : LOW_PRIORITY_MEMORY))
							.withAutoRemove(true)
							.withPidsLimit(8192L)
							.withRestartPolicy(RestartPolicy.noRestart())
					).exec();

			containerId = container.getId();
			LOGGER.info("Created container with id: {}", container.getId());

			dockerClient.startContainerCmd(container.getId()).exec();
			LOGGER.info("Started container with id: {}", container.getId());

			StringBuilder readStd = new StringBuilder();

			dockerClient
					.logContainerCmd(containerName)
					.withStdErr(true)
					.withStdOut(true)
					.withFollowStream(true)
					.withSince(0)
					.exec(new ResultCallbackTemplate<LogContainerResultCallback, Frame>() {
						@Override
						public void onNext(Frame frame) {
							if (!submission.getSystemExtra().contains("noStd")) {
								readStd.append(new String(frame.getPayload()));
							}
						}

						@Override
						public void onComplete() {
							submission.setResult(truncateLogs(readStd.toString()));
							done = true;
							LOGGER.info("DockerTestRunner for user {} with slug {} finished", submission.getUniid(), slug);
							super.onComplete();
						}
					});


			int seconds = submission.getDockerTimeout();
			while (!done) {
				TimeUnit.SECONDS.sleep(1);
				seconds--;
				if (seconds == 0) {
					throw new DockerTimeoutException("Timed out");
				}
			}

		} catch (Exception e) {
			throw new DockerRunnerException("Exception in docker, message: " + e.getMessage());
		}
		return containerId;
	}

	public void cleanup() {
		if (dockerClient != null && container != null) {

			try {
				dockerClient.stopContainerCmd(container.getId()).exec();
				LOGGER.info("Stopped container: {}", containerId);
			} catch (Exception stop) {
				LOGGER.info("Container {} has already been stopped", containerId);
			}

			try {
				dockerClient.killContainerCmd(containerId).exec();
				LOGGER.info("Killed container: {}", containerId);
			} catch (Exception stop) {
				LOGGER.info("Container {} has already been killed", containerId);
			}

			try {
				dockerClient.removeContainerCmd(container.getId()).exec();
				LOGGER.info("Removed container: {}", container.getId());
			} catch (Exception remove) {
				LOGGER.error("Container {} has already been removed", containerId);
			}

			LOGGER.info("Cleaned up for submission: {}", submission.getHash());

			for (Volume volume : volumes) {
				dockerClient.removeVolumeCmd(volume.toString());
			}

			LOGGER.info("Cleaned up volumes");
		}
	}

	public static boolean isPriorityJob(Submission submission) {
		return submission.getPriority() > 7;
	}


	public static String truncateLogs(String string) {
		String cut = Stream.of(string.split("\n"))
				.map(s -> s.substring(0, Math.min(s.length(), 10000)))
				.limit(2000)
				.collect(Collectors.joining("\n"));

		return cut.substring(0, Math.min(cut.length(), 100000));
	}

	private static String getImage(DockerClient dockerClient, String image) {

		try {
			ImageCheck imageCheck = new ImageCheck(dockerClient, image);
			imageCheck.invoke();
			return imageCheck.getTester().getId();
		} catch (Exception e) {
			throw new ImageNotFoundException(e.getMessage());
		}
	}

	@SneakyThrows
	private static void copyFiles(String from, String to, String folder, List<FileDTO> source, String error) {
		try {
			if (folder != null) {
				FileUtils.copyDirectory(new java.io.File(from), new java.io.File(to));
			} else {
				for (FileDTO file : source) {
					copyFilesFromSource(to, file);
				}
			}
		} catch (IOException e) {
			LOGGER.error(error);
			throw new IOException(e.getMessage());
		}
	}

	private static void copyFilesFromSource(String tempStudent, FileDTO file) throws IOException {
		String temp;
		try {
			temp = file.getPath().substring(file.getPath().indexOf("\\"));
		} catch (Exception e) {
			temp = file.getPath().substring(file.getPath().indexOf("/"));
		}

		java.io.File path = new java.io.File(String.format("%s/%s", tempStudent, temp));
		path.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(path);
		writer.write(file.getContents());
		writer.close();
	}
}
