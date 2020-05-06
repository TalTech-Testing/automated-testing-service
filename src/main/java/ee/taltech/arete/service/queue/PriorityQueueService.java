package ee.taltech.arete.service.queue;

import ee.taltech.arete.domain.Submission;

import java.util.List;
import java.util.Set;

public interface PriorityQueueService {

	void enqueue(Submission submission);

	void runJob();

	void killThread(Submission submission, List<String> outputs);

	Integer getJobsRan();

	Integer getQueueSize();

	void halt() throws InterruptedException;

	void halt(int maxAllowedJobs) throws InterruptedException;

	void go();

	List<Submission> getActiveSubmissions();

	Set<Integer> getCores();

	void clearCache();
}
