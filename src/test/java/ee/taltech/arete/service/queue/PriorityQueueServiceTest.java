package ee.taltech.arete.service.queue;

import ee.taltech.arete.domain.Submission;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static ee.taltech.arete.initializers.SubmissionInitializer.getFullSubmission;

@RunWith(SpringRunner.class)
@SpringBootTest
class PriorityQueueServiceTest {

	@Autowired
	PriorityQueueService priorityQueueService;

	@Test
	void enqueue() {
		priorityQueueService.enqueue(getFullSubmission());
		assert priorityQueueService.getQueueSize() == 1;
	}

	@Test
	void runJob() throws InterruptedException {

		int jobSets = 2;

		for (int i = 0; i < jobSets; i++) {
			Submission submission = getFullSubmission();
			priorityQueueService.enqueue(submission);
		}

//		Optional<Submission> submission = priorityQueueService.runJob();
//		assert submission.isPresent();
//		int lastPriority = submission.get().getPriority();
//		long lastTimestamp = submission.get().getTimestamp();
//
//		while (true) {
//			Optional<Submission> submissionNew = priorityQueueService.runJob();
//			if (submissionNew.isEmpty()) {
//				break;
//			}
//			if (submissionNew.get().getPriority() != lastPriority) {
//				assert submissionNew.get().getPriority() < lastPriority;
//				lastPriority = submissionNew.get().getPriority();
//				lastTimestamp = submissionNew.get().getTimestamp();
//			} else {
//				assert submissionNew.get().getTimestamp() >= lastTimestamp;
//				lastPriority = submissionNew.get().getPriority();
//				lastTimestamp = submissionNew.get().getTimestamp();
//			}
//		}

		while (priorityQueueService.getJobsRan() < 2) {
			TimeUnit.SECONDS.sleep(1);
		}
	}
}