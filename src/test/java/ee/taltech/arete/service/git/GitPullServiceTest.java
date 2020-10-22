package ee.taltech.arete.service.git;

import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.service.git.GitPullService;
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

	int port = 8080;

	@Test
	void pullJobGithub() {
		// given, when
		Submission submission = getGitPullEndpointSubmissionGithub(String.format("http://localhost:%s", port));

		// then
		assert gitPullService.repositoryMaintenance(submission);

	}

	@Test
	void pullJobGitlab() {
		// given, when
		Submission submission = getGitPullEndpointSubmissionGitlab(String.format("http://localhost:%s", port));

		// then
		assert gitPullService.repositoryMaintenance(submission);

	}

}