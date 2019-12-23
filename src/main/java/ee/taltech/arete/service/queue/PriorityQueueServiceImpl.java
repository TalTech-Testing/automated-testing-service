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

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@EnableAsync
public class PriorityQueueServiceImpl implements PriorityQueueService {

	private static final Integer MAX_JOBS = Runtime.getRuntime().availableProcessors();
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
	private HashMap<Submission, Integer> coolDown = new HashMap<>();
	private List<Submission> lowPriority = new ArrayList<>();
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
		submissionService.saveSubmission(submission);
		activeSubmissions.remove(submission);
		jobsRan++;
		activeRunningJobs--;
		threads.add(submission.getThread());
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

	@Async
	@Override
	@Scheduled(fixedRate = 1000)
	public void timer() {
		List<Submission> done = new ArrayList<>();
		for (Submission job : lowPriority) {
			coolDown.put(job, coolDown.get(job) - 1000);
			if (coolDown.get(job) == 0) {
				coolDown.remove(job);
				done.add(job);
				job.setPriority(5);
			}
		}
		lowPriority.removeAll(done);
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
	@Async
	@Scheduled(fixedRate = 100)
	public void runJob() {
		if (!halted && getQueueSize() != 0) {
			if (activeRunningJobs < MAX_JOBS) {

				Submission job = submissionPriorityQueue.poll();
				assert job != null;

				if (job.getPriority() < 8 && activeSubmissions.stream().anyMatch(o -> o.getUniid().equals(job.getUniid()))) {
					job.setPriority(4); // Mild punish for spam pushers.

					if (!coolDown.containsKey(job)) {
						lowPriority.add(job);
					}


					coolDown.put(job, 300000);
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
//					e.printStackTrace();
					LOGGER.error("Job failed with message: {}", e.getMessage());
					killThread(job);
				}

			}
		}
	}
}
