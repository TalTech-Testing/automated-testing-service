//package ee.taltech.arete.service.queue;
//
//import ee.taltech.arete.domain.Submission;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.concurrent.TimeUnit;
//
//import static ee.taltech.arete.initializers.SubmissionInitializer.getFullSubmissionJava;
//import static ee.taltech.arete.initializers.SubmissionInitializer.getFullSubmissionPython;
//
//@AutoConfigureTestDatabase
//@RunWith(SpringRunner.class)
//@SpringBootTest
//class PriorityQueueServiceTest {
//
//	@Autowired
//	PriorityQueueService priorityQueueService;
//
//	@Test
//	void enqueue() {
//		// given, when
//		priorityQueueService.enqueue(getFullSubmissionJava());
////		priorityQueueService.enqueue(getFullSubmissionPython());
//
//		// then
//		assert priorityQueueService.getQueueSize() == 1;
//	}
//
//	@Test
//	void runJob() throws InterruptedException {
//		// given
//		int jobSets = 3;
//
//		// when
//		for (int i = 0; i < jobSets; i++) {
//			Submission submission = getFullSubmissionPython();
//			priorityQueueService.enqueue(submission);
//		}
//
//		while (priorityQueueService.getJobsRan() < 4) {
//			TimeUnit.SECONDS.sleep(1);
//		}
//
//		//TODO Check something
//	}
//}