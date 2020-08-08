package ee.taltech.arete.service.git;

import ee.taltech.arete.domain.Submission;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static ee.taltech.arete.initializers.SubmissionInitializer.getGitPullEndpointSubmissionGithub;
import static ee.taltech.arete.initializers.SubmissionInitializer.getGitPullEndpointSubmissionGitlab;

@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest
class GitPullServiceTest {

	@Autowired
	private GitPullService gitPullService;

	@Test
	void pullJobGithub() {
		// given, when
		Submission submission = getGitPullEndpointSubmissionGithub();

		// then
		assert gitPullService.repositoryMaintenance(submission);

	}

	@Test
	void pullJobGitlab() {
		// given, when
		Submission submission = getGitPullEndpointSubmissionGitlab();

		// then
		assert gitPullService.repositoryMaintenance(submission);

	}

}