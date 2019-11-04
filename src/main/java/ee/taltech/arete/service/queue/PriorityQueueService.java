package ee.taltech.arete.service.queue;

import ee.taltech.arete.domain.Submission;

import java.util.Optional;

public interface PriorityQueueService {

	void enqueue(Submission submission);

	Integer getQueueSize();

	Optional<Submission> runJob();

}
