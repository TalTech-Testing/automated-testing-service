package ee.taltech.arete.service.queue;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.runner.JobRunnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.PriorityQueue;

@Service
@EnableAsync
public class PriorityQueueServiceImpl implements PriorityQueueService {

	@Autowired
	@Lazy
	private JobRunnerService jobRunnerService;

	private static final Integer MAX_JOBS = 5;
	private Integer runningJobs = 0;
	private Integer successfulJobsRan = 0;

	private static Logger LOGGER = LoggerFactory.getLogger(PriorityQueueService.class);

	private PriorityQueue<Submission> submissionPriorityQueue = new PriorityQueue<>(Comparator
			.comparingInt(Submission::getPriority)
			.reversed()
			.thenComparing(Submission::getTimestamp));

	@Override
	public void enqueue(Submission submission) {
		submissionPriorityQueue.add(submission);
		runJob();
	}

	@Override
	public void killThread() {
		runningJobs--;
		successfulJobsRan++;
	}

	@Override
	public Integer getSuccessfulJobsRan() {
		return successfulJobsRan;
	}

	@Override
	public Integer getQueueSize() {
		return submissionPriorityQueue.size();
	}

	@Override
	@Async
	@Scheduled(fixedRate = 1000)
	public void runJob() {
		if (getQueueSize() != 0) {
			if (runningJobs < MAX_JOBS) {

				runningJobs++;
				Thread thread = new Thread(() -> {
					Submission job = submissionPriorityQueue.remove();
					LOGGER.info("Running job for {} with hash {}", job.getUniid(), job.getHash());
					jobRunnerService.runJob(job);
				});

				thread.start();
			}
		}
	}
}
