package ee.taltech.arete.service.queue;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.runner.JobRunnerService;
import ee.taltech.arete.service.submission.SubmissionService;
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
import java.util.concurrent.TimeUnit;

@Service
@EnableAsync
public class PriorityQueueServiceImpl implements PriorityQueueService {

	private static final Integer MAX_JOBS = Math.max(Runtime.getRuntime().availableProcessors() / 2, 1);
	private static Logger LOGGER = LoggerFactory.getLogger(PriorityQueueService.class);

	@Autowired
	private SubmissionService submissionService;

	@Autowired
	@Lazy
	private JobRunnerService jobRunnerService;

	private Boolean halted = true;
	private Integer jobsRan = 0;
	private Integer activeRunningJobs = 0;
	private List<Integer> threads = new ArrayList<>();
	private List<Submission> activeSubmissions = new ArrayList<>();
	private PriorityQueue<Submission> submissionPriorityQueue = new PriorityQueue<>(Comparator
			.comparingInt(Submission::getPriority)
			.reversed()
			.thenComparing(Submission::getTimestamp));

	public PriorityQueueServiceImpl() {
		for (int i = 1; i <= MAX_JOBS; i++) {
			threads.add(i);
		}
	}

	@Override
	public void enqueue(Submission submission) {
		submissionPriorityQueue.add(submission);
	}

	@Override
	public void killThread(Submission submission) {
		activeRunningJobs--;
		jobsRan++;
		activeSubmissions.remove(submission);
		threads.add(submission.getThread());
		try {
			submissionService.saveSubmission(submission);
		} catch (Exception e) {
			LOGGER.error("Failed to save result to DB: {}", e.getMessage());
		}
		LOGGER.info("All done for submission on thread: {}", submission.getThread());
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
	public void halt() throws InterruptedException {
		halted = true;
		while (activeRunningJobs != 0) {
			TimeUnit.SECONDS.sleep(1);
		}
	}

	@Override
	public void go() {
		halted = false;
	}

	@Override
	public List<Submission> getActiveSubmissions() {
		return activeSubmissions;
	}

	@Override
	@Async
	@Scheduled(fixedRate = 100)
	public void runJob() {
		if (!halted && getQueueSize() != 0 && activeRunningJobs < MAX_JOBS) {

			Submission job = submissionPriorityQueue.poll();
			assert job != null;

			if (job.getPriority() < 8 && activeSubmissions.stream().anyMatch(o -> o.getUniid().equals(job.getUniid()))) {
				job.setPriority(4); // Mild punish for spam pushers.

				submissionPriorityQueue.add(job);
				return;
			}

			activeRunningJobs++;
			activeSubmissions.add(job);

			LOGGER.info("active: {}, queue: {}, ran: {}", activeRunningJobs, getQueueSize(), jobsRan);

			LOGGER.info("Running job for {} with hash {}", job.getUniid(), job.getHash());
			job.setThread(threads.remove(0));

			try {
				jobRunnerService.runJob(job);
			} catch (Exception e) {
				LOGGER.error("Job failed with message: {}", e.getMessage());
			}

			killThread(job);

		}
	}
}
