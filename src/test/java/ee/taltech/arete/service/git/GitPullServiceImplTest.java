package ee.taltech.arete.service.git;

import ee.taltech.arete.domain.Submission;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

import static ee.taltech.arete.initializers.SubmissionInitializer.getControllerEndpointSubmission;

@RunWith(SpringRunner.class)
@SpringBootTest
class GitPullServiceImplTest {

	@Autowired
	private GitPullService gitPullService;

	@Test
	void pullJob() throws InterruptedException {
		Submission submission = getControllerEndpointSubmission();
		gitPullService.repositoryMaintenance(submission);
		gitPullService.repositoryMaintenance(submission);
		gitPullService.resetHead(submission);
		gitPullService.repositoryMaintenance(submission);

		String home = System.getenv().getOrDefault("ARETE_HOME", System.getenv("HOME") + "/arete");
		String tester = String.format("%s/tests/%s/%s", home, "iti0102-2019", "ex02_binary");
		String tempTester = String.format("%s/input_and_output/%s/tester", home, 0); // Slug into temp folder

		try {
			FileUtils.copyDirectory(new File(tester), new File(tempTester));
		} catch (IOException ignored) {
		}

	}

}