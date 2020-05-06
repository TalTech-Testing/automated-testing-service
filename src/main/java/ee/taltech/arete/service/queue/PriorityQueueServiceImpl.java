package ee.taltech.arete.service.queue;

import com.sun.management.OperatingSystemMXBean;
import ee.taltech.arete.configuration.DevProperties;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.runner.JobRunnerService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@EnableAsync
public class PriorityQueueServiceImpl implements PriorityQueueService {

	private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
	private final Logger LOGGER = LoggerFactory.getLogger(PriorityQueueService.class);

	private final DevProperties devProperties;

	private final JobRunnerService jobRunnerService;
	private final Set<Integer> threads = new HashSet<>();
	private final List<Submission> activeSubmissions = new ArrayList<>();
	private final PriorityQueue<Submission> submissionPriorityQueue = new PriorityQueue<>(Comparator
			.comparingInt(Submission::getPriority)
			.reversed()
			.thenComparing(Submission::getTimestamp));
	private Boolean halted = true;
	private Integer jobsRan = 0;
	private Integer stuckQueue = 3000; // just some protection against stuck queue

	@Lazy
	public PriorityQueueServiceImpl(DevProperties devProperties, JobRunnerService jobRunnerService) {
		for (int i = 0; i < devProperties.getParallelJobs(); i++) {
			threads.add(i);
		}
		this.devProperties = devProperties;
		this.jobRunnerService = jobRunnerService;
	}

	private boolean isCPUAvaiable() {
		return osBean.getSystemCpuLoad() < devProperties.getMaxCpuUsage() && devProperties.getParallelJobs() > activeSubmissions.size();
	}

	@Override
	public void enqueue(Submission submission) {
		submissionPriorityQueue.add(submission);
	}

	public void killThread(Submission submission, List<String> outputs) {
		jobsRan++;
		activeSubmissions.remove(submission);
		threads.add(submission.getThread());

		for (String output : outputs) {
			jobRunnerService.clearInputAndOutput(submission, output);
		}

		try {

			FileUtils.cleanDirectory(new File(String.format("input_and_output/%d/student", submission.getThread())));
			FileUtils.cleanDirectory(new File(String.format("input_and_output/%d/tester", submission.getThread())));

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
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
		int antiStuck = 30;
		while (activeSubmissions.size() != 0 && antiStuck != 0) {
			TimeUnit.SECONDS.sleep(1);
			antiStuck--;
		}
	}

	@Override
	public void halt(int maxAllowedJobs) throws InterruptedException {
		halted = true;
		int antiStuck = 30;
		while (activeSubmissions.size() > maxAllowedJobs && antiStuck != 0) {
			TimeUnit.SECONDS.sleep(1);
			antiStuck--;
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
	public Set<Integer> getCores() {
		return threads;
	}

	@Override
	@Async
	@Scheduled(fixedRate = 1000)
	public void clearCache() {

		if (activeSubmissions.size() == 0) {
			for (int i = 0; i < devProperties.getParallelJobs(); i++) {
				threads.add(i);
			}
		}

		try {
			for (Submission submission : getActiveSubmissions()) {
				if (submission.getTimestamp() + Math.min(submission.getDockerTimeout() + 10, stuckQueue) * 1000 < System.currentTimeMillis()) {
					killThread(submission, new ArrayList<>());
				}
			}
		} catch (ConcurrentModificationException ignored) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	@Async
	@Scheduled(fixedRate = 100)
	public void runJob() {

		if (threads.size() == 0 || submissionPriorityQueue.size() == 0) {
			return;
		}

		int thread;

		for (thread = 0; thread < devProperties.getParallelJobs(); thread++) {
			if (threads.remove(thread)) {
				break;
			}
		}

		final int finalThread = thread;
		threads.remove(finalThread);

		if (activeSubmissions.stream().anyMatch(x -> x.getThread().equals(finalThread))) {
			return;
		}

		if (halted) {
			stuckQueue--;
		} else {
			stuckQueue = 300;
		}

		if (stuckQueue <= 0) {
			halted = false;
		}

		if (!halted && getQueueSize() != 0 && isCPUAvaiable()) {

			Submission job = submissionPriorityQueue.poll();

			if (job == null) {
				threads.add(finalThread);
				return;
			}

			if (job.getPriority() < 8 && job.getUniid() != null && activeSubmissions.stream().anyMatch(o -> o.getUniid().equals(job.getUniid()))) {
				job.setPriority(4); // Mild punish for spam pushers.

				submissionPriorityQueue.add(job);
				threads.add(finalThread);
				return;
			}

			job.setTimestamp(System.currentTimeMillis());
			activeSubmissions.add(job);

			LOGGER.info("active: {}, queue: {}, ran: {}", activeSubmissions.size(), getQueueSize(), jobsRan);

			LOGGER.info("Running job for {} with hash {}", job.getUniid(), job.getHash());

			job.setThread(finalThread);

			List<String> outputs = new ArrayList<>();

			try {
				outputs.addAll(jobRunnerService.runJob(job));
			} catch (Exception e) {
				LOGGER.error("Job failed with message: {}", e.getMessage());
			}

			killThread(job, outputs);
		}
	}
}
