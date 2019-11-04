package ee.taltech.arete.service.queue;

import ee.taltech.arete.domain.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;

@Service
public class PriorityQueueServiceImpl implements PriorityQueueService {

	private static Logger LOGGER = LoggerFactory.getLogger(PriorityQueueService.class);

	private PriorityQueue<Submission> submissionPriorityQueue = new PriorityQueue<>(Comparator
			.comparingInt(Submission::getPriority)
			.reversed()
			.thenComparing(Submission::getTimestamp));

	@Override
	public void enqueue(Submission submission) {
		submissionPriorityQueue.add(submission);
	}

	@Override
	public Integer getQueueSize() {
		return submissionPriorityQueue.size();
	}

	@Override
	public Optional<Submission> runJob() {
		if (getQueueSize() == 0) {
			return Optional.empty();
		} else {
			Submission job = submissionPriorityQueue.remove();
			LOGGER.info("Running job: {}", job);
			return Optional.of(job);
		}
	}
}
