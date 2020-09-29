package ee.taltech.arete.service.runner;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.JobRunnerService;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashSet;

import static ee.taltech.arete.initializers.SubmissionInitializer.getGitPullEndpointSubmissionGitlab;

@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest
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