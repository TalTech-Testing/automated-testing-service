package ee.taltech.arete.service.runner;

import ee.taltech.arete_testing_service.AreteApplication;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.service.JobRunnerService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashSet;

import static ee.taltech.arete.initializers.SubmissionInitializer.getGitPullEndpointSubmissionGitlab;

@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AreteApplication.class)
class JobRunnerServiceTest {

	@Autowired
	private JobRunnerService jobRunnerService;

	int port = 8080;

	@Test
	void formatSlugs() {
		// given
		Submission submission = getGitPullEndpointSubmissionGitlab(String.format("http://localhost:%s", port));

		// when
		jobRunnerService.formatSlugs(submission);

		// then
		assert submission.getSlugs().containsAll(new HashSet<>(Arrays.asList("EX01IdCode", "TK/tk_tsükkel_1")));
	}

	@Test
	void revertToInitialMail() {
		// given
		Submission submission = getGitPullEndpointSubmissionGitlab(String.format("http://localhost:%s", port));
		submission.setEmail("enrico.vompa@gmail.com");
		String initial = submission.getEmail();

		// when
		jobRunnerService.modifyEmail(submission, initial);

		// then
		assert submission.getEmail().equals("envomp@ttu.ee");
	}

}