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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Service
@EnableAsync
public class PriorityQueueServiceImpl implements PriorityQueueService {

	private static final Integer MAX_JOBS = 8;
	private static Logger LOGGER = LoggerFactory.getLogger(PriorityQueueService.class);
	@Autowired
	@Lazy
	private JobRunnerService jobRunnerService;
	private Integer jobsRan = 0;
	private Integer activeRunningJobs = 0;
	private Integer counter = 0;
	private List<Submission> activeSubmissions = new ArrayList<>();
	private PriorityQueue<Submission> submissionPriorityQueue = new PriorityQueue<>(Comparator
			.comparingInt(Submission::getPriority)
			.reversed()
			.thenComparing(Submission::getTimestamp));

	@Override
	public void enqueue(Submission submission) {
		submissionPriorityQueue.add(submission);
	}

	@Override
	public void killThread(Submission submission) {
		LOGGER.info("All done for submission on thread: {}", submission.getThread());
		activeSubmissions.remove(submission);
		jobsRan++;
		activeRunningJobs--;
	}

	@Override
	public Integer getJobsRan() {
		return jobsRan;
	}

	@Override
	public Integer getQueueSize() {
		return submissionPriorityQueue.size();
	}

	@Override
	@Async
	@Scheduled(fixedRate = 100)
	public void runJob() {
		if (getQueueSize() != 0) {
			if (activeRunningJobs < MAX_JOBS) {

				Submission job = submissionPriorityQueue.poll();
				assert job != null;

				if (job.getPriority() < 8 && activeSubmissions.stream().anyMatch(o -> o.getUniid().equals(job.getUniid()))) {
					job.setPriority(4); // Mild punish for spam pushers.
					submissionPriorityQueue.add(job);
					return;
				}

				counter++;
				activeRunningJobs++;
				activeSubmissions.add(job);

				LOGGER.info("active: {}, queue: {}, ran: {}", activeRunningJobs, getQueueSize(), jobsRan);

				LOGGER.info("Running job for {} with hash {}", job.getUniid(), job.getHash());
				job.setThread(counter % MAX_JOBS);

				Thread thread = new Thread(() -> {
					try {
						jobRunnerService.runJob(job);
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
						killThread(job);
					}
				});

				thread.start();

			}
		}
	}
}
