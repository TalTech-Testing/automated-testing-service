package ee.taltech.arete.service.runner;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.JobRunnerService;
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
@SpringBootTest
class JobRunnerServiceTest {

	@Autowired
	private JobRunnerService jobRunnerService;

	@Test
	void formatSlugs() {
		// given
		Submission submission = getGitPullEndpointSubmissionGitlab();

		// when
		jobRunnerService.formatSlugs(submission);

		// then
		System.out.println(submission.getSlugs());
		assert submission.getSlugs().containsAll(new HashSet<>(Arrays.asList("EX01IdCode", "TK/tk_tsükkel_1")));
	}

}