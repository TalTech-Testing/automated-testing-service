package ee.taltech.arete.service.submission;

import ee.taltech.arete.domain.Submission;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class SubmissionServiceTest {

	private Submission submission = new Submission("envomp", "hash", "python", "neti.ee", new String[]{"style"});

	@Autowired
	private SubmissionService submissionService;

	@Test
	void getSubmissions() {
		submissionService.saveSubmission(submission);
		System.out.println(submissionService.getSubmissions());
		assert submissionService.getSubmissions().size() == 1;
	}

	@Test
	void getSubmissionByHash() {
		submissionService.saveSubmission(submission);
		System.out.println(submissionService);
		assert submissionService.getSubmissionByHash("hash").getUniid().equals("envomp");
	}
}