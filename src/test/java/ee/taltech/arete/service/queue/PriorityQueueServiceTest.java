//package ee.taltech.arete.service.queue;
//
//import ee.taltech.arete.domain.Submission;
//import ee.taltech.arete.service.SubmissionService;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.concurrent.TimeUnit;
//
//import static ee.taltech.arete.initializers.SubmissionInitializer.getGitPullEndpointSubmission;
//
//@AutoConfigureTestDatabase
//@RunWith(SpringRunner.class)
//@SpringBootTest
//class PriorityQueueServiceTest {
//
//	@Autowired
//	PriorityQueueService priorityQueueService;
//
//	@Autowired
//	SubmissionService submissionService;
//
////	@Test
////	void enqueue() {
////		// given, when
////		priorityQueueService.enqueue(getFullSubmissionJava());
//////		priorityQueueService.enqueue(getFullSubmissionPython());
////
////		// then
////		assert priorityQueueService.getQueueSize() == 1;
////	}
//
//	@Test
//	void runJob() throws InterruptedException {
//		// given
//		int jobSets = 100;
//
//		// when
//		for (int i = 0; i < jobSets; i++) {
//			Submission submission = getGitPullEndpointSubmission();
//			submissionService.populateAsyncFields(submission);
//			submission.setReturnUrl(String.valueOf(i));
//			priorityQueueService.enqueue(submission);
//		}
//
//		while (priorityQueueService.getJobsRan() < jobSets) {
//			TimeUnit.SECONDS.sleep(1);
//		}
//
//		//TODO Check something
//	}
//}