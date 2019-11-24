package ee.taltech.arete.service.submission;

import ee.taltech.arete.domain.Submission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static ee.taltech.arete.initializers.SubmissionInitializer.endTest;
import static ee.taltech.arete.initializers.SubmissionInitializer.getControllerEndpointSubmission;

@RunWith(SpringRunner.class)
@SpringBootTest
class SubmissionServiceTest {

	private Submission submission = getControllerEndpointSubmission();

	@Autowired
	private SubmissionService submissionService;

	@Test
	void getSubmissions() {
		submissionService.saveSubmission(submission);
		assert submissionService.getSubmissions().size() == 1;
	}

	@Test
	void getSubmissionByHash() {
		submissionService.saveSubmission(submission);
		assert submissionService.getSubmissionByHash(submission.getHash()).getUniid().equals(submission.getUniid());
	}

}
