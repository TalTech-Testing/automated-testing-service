package ee.taltech.arete.service.git;

import ee.taltech.arete.domain.Submission;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static ee.taltech.arete.initializers.SubmissionInitializer.getControllerEndpointSubmission;

@RunWith(SpringRunner.class)
@SpringBootTest
class GitPullServiceImplTest {

	@Autowired
	private GitPullService gitPullService;

	@Test
	void pullJob() {
		Submission submission = getControllerEndpointSubmission();
		gitPullService.repositoryMaintenance(submission);
		gitPullService.repositoryMaintenance(submission);
		gitPullService.resetHeadAndPull(submission);

	}

}