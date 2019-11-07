package ee.taltech.arete.service.runner;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static ee.taltech.arete.initializers.SubmissionInitializer.getFullSubmission;

@RunWith(SpringRunner.class)
@SpringBootTest
class JobRunnerServiceImplTest {

	@Autowired
	JobRunnerService jobRunnerService;

	@Test
	void runJob() {
		jobRunnerService.runDocker(getFullSubmission());
	}
}