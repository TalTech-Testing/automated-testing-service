package ee.taltech.arete.service.queue;

import ee.taltech.arete.domain.Submission;

public interface PriorityQueueService {

	void enqueue(Submission submission);

	void runJob();

	void killThread();

	Integer getSuccessfulJobsRan();

	Integer getQueueSize();

}
