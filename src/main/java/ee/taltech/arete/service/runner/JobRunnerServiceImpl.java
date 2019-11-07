package ee.taltech.arete.service.runner;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import ee.taltech.arete.domain.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;


@Service
public class JobRunnerServiceImpl implements JobRunnerService {

	private static Logger LOGGER = LoggerFactory.getLogger(JobRunnerServiceImpl.class);

	@Override
	public void runJob(Submission submission) {
		System.out.println("RUNNING JOB " + submission);
		pullJobRequirements(submission);
	}

	public void pullJobRequirements(Submission submission) {
//        for () {
//            submission.setSlug("TODO");
//      		runDocker(submission);
//        }
	}

	public void runDocker(Submission testJob) {
		DockerClient dockerClient = null;
		CreateContainerResponse container = null;
		String imageId;
//
//	    String hash = RandomStringUtils.random(10, true, true);
		String command = String.format(
				"docker build --build-arg uniid=%s --build-arg slug=%s -f Dockerfile-java-test-job -t %s . && docker run -it %s",
				testJob.getUniid(),
				testJob.getSlug(),
				testJob.getHash(),
				testJob.getHash());
		List<String> dockerBuild = Arrays.asList(command.split(" "));
		LOGGER.info("Docker configuration: {}", dockerBuild);

		try {

			DefaultDockerClientConfig.Builder config = DefaultDockerClientConfig.createDefaultConfigBuilder();
			dockerClient = DockerClientBuilder.getInstance(config).build();

			imageId =
					dockerClient.buildImageCmd()
							.withBuildArg("uniid", testJob.getUniid())
							.withBuildArg("slug", testJob.getSlug())
							.withBaseDirectory(new File("./"))
							.withDockerfile(new File("Dockerfile-java-test-job"))
							.withPull(true)
							.withTag(testJob.getHash())
							.withNoCache(false)
							.exec(new BuildImageResultCallback())
							.awaitImageId();
			LOGGER.info("Created image with id: {}", imageId);

			container = dockerClient.createContainerCmd(testJob.getHash() + ":latest").exec();
			LOGGER.info("Created container with id: {}", container.getId());

			dockerClient.startContainerCmd(container.getId()).exec();
			LOGGER.info("Started container with id: {}", container.getId());

			InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
			System.out.println(inspectContainerResponse);
			System.out.println();

//			dockerClient.startContainerCmd(container.getId()).exec();
//			System.out.println(dockerClient.waitContainerCmd(container.getId()).exec(new WaitContainerResultCallback()).awaitStatusCode());
//
//			inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
//			System.out.println(inspectContainerResponse);

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Job failed with exception: {}", e.getMessage());
		}

		try {

//			if (dockerClient != null && container != null) {
//
//				LOGGER.info("Stopping container: {}", container.getId());
//				try {
//					dockerClient.stopContainerCmd(container.getId()).withTimeout(20).exec();
//				} catch (Exception stop) {
//					LOGGER.error("Container {} didnt start to begin with", container.getId());
//				}
//
//				LOGGER.info("Removing container: {}", container.getId());
//				try {
//					dockerClient.removeContainerCmd(container.getId()).exec();
//				} catch (Exception image) {
//					LOGGER.error("Container {} wasn't created to begin with", testJob.getHash());
//				}
//			}

		} catch (Exception finish) {
			LOGGER.error("Container {} failed to start", container.getId());
		}
	}

}


//TODO dis
//docker build --build-arg uniid=envomp --build-arg slug=EX01IdCode -f Dockerfile-java-test-job -t somehash . && docker run -it somehash


// java -jar tester-java/build/libs/studenttester-core-2.0.jar -c envomp/iti0202-2019/EX01IdCode/ -t iti0202-2019/ex/EX01IdCode/ -jsontxt -r REPORT,CHECKSTYLE,JAR,COMPILER,TESTNG

// docker build --build-arg uniid=envomp --build-arg slug=EX01IdCode -f Dockerfile-java-test-job -t somehash . && docker run -it somehash