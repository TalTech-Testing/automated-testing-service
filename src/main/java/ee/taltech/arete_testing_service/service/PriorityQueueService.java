package ee.taltech.arete_testing_service.service;

import com.sun.management.OperatingSystemMXBean;
import ee.taltech.arete_testing_service.configuration.DevProperties;
import ee.taltech.arete_testing_service.domain.Submission;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@EnableAsync
public class PriorityQueueService {

	private PriorityQueue<Submission> emptyQueue() {
		return new PriorityQueue<>(Comparator
				.comparingInt(Submission::getPriority)
				.reversed()
				.thenComparing(Submission::getReceivedTimestamp));
	}

	private OperatingSystemMXBean getOsBean() {
		return ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
	}

	private final OperatingSystemMXBean osBean = getOsBean();
	private final Logger logger;
	private final DevProperties devProperties;
	private final JobRunnerService jobRunnerService;

	private final PriorityQueue<Submission> submissionPriorityQueue = emptyQueue();
	private static final CopyOnWriteArrayList<Submission> activeSubmissions = new CopyOnWriteArrayList<>();
	private static Boolean halted = true;
	private static Integer jobsRan = 0;
	private static Integer stuckQueue = 3000; // just some protection against stuck queue

	public Integer getJobsRan() {
		return jobsRan;
	}

	public static void halt() throws InterruptedException {
		halted = true;
		int antiStuck = 30;
		while (activeSubmissions.size() != 0 && antiStuck != 0) {
			TimeUnit.SECONDS.sleep(1);
			antiStuck--;
		}
	}

	public static void halt(int maxAllowedJobs) throws InterruptedException {
		halted = true;
		int antiStuck = 30;
		while (activeSubmissions.size() > maxAllowedJobs && antiStuck != 0) {
			TimeUnit.SECONDS.sleep(1);
			antiStuck--;
		}
	}

	public static void go() {
		halted = false;
	}

	@Async
	@Scheduled(fixedRate = 10000)
	public void clearCache() {

		try {
			for (Submission submission : getActiveSubmissions()) {
				if (submission.getReceivedTimestamp() + Math.min(submission.getDockerTimeout() + 10, stuckQueue) * 1000 < System.currentTimeMillis()) {
					killThread(submission);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Submission> getActiveSubmissions() {
		return activeSubmissions;
	}

	public void killThread(Submission submission) {
		jobsRan++;
		activeSubmissions.remove(submission);

		try {
			TimeUnit.SECONDS.sleep(10); // keep files for a little bit so mail can send them and prevent spam pushing
			FileUtils.deleteDirectory(new File(String.format("input_and_output/%s", submission.getHash())));
		} catch (Exception e) {
			logger.error("Failed deleting directory after killing thread: {}", e.getMessage());
		}

		logger.info("All done for submission on thread: {}", submission.getHash());
	}

	@Async
	@Scheduled(fixedRate = 100)
	public void runJob() {

		if (submissionPriorityQueue.size() == 0) {
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
				return;
			}

			if (job.getPriority() < 8 && job.getUniid() != null && activeSubmissions.stream().anyMatch(o -> o.getUniid().equals(job.getUniid()))) {
				job.setPriority(4); // Mild punish for spam pushers.

				enqueue(job);
				return;
			}

			if (job.getTimestamp() == null) {
				job.setTimestamp(System.currentTimeMillis());
			}

			activeSubmissions.add(job);

			logger.info("active: {}, queue: {}, ran: {}", activeSubmissions.size(), getQueueSize(), jobsRan);

			logger.info("Running job for {} with hash {}", job.getUniid(), job.getHash());

			try {
				jobRunnerService.runJob(job);
			} catch (Exception e) {
				logger.error("Job failed with message: {}", e.getMessage());
			} finally {
				killThread(job);
			}

		}
	}

	public Integer getQueueSize() {
		return submissionPriorityQueue.size();
	}

	private boolean isCPUAvaiable() {
		return osBean.getSystemCpuLoad() < devProperties.getMaxCpuUsage() && devProperties.getParallelJobs() > activeSubmissions.size();
	}

	@SneakyThrows
	public void enqueue(Submission submission) {
		for (int i = 0; i < 10; i++) {
			try {
				submissionPriorityQueue.add(submission);
				break;
			} catch (Exception ignored) {
				TimeUnit.SECONDS.sleep(1);
			}
		}
	}
}
