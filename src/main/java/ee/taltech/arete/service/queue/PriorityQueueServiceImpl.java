package ee.taltech.arete.service.queue;

import com.sun.management.OperatingSystemMXBean;
import ee.taltech.arete.configuration.DevProperties;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.runner.JobRunnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

@Service
@EnableAsync
public class PriorityQueueServiceImpl implements PriorityQueueService {

    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private final Logger LOGGER = LoggerFactory.getLogger(PriorityQueueService.class);

    private DevProperties devProperties;

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
    private Integer stuckQueue = 300; // just some protection against stuck queue

    @Lazy
    public PriorityQueueServiceImpl(DevProperties devProperties, JobRunnerService jobRunnerService) {
        for (int i = 1; i < devProperties.getUsableCores(); i++) {
            threads.add(i);
        }
        this.devProperties = devProperties;
        this.jobRunnerService = jobRunnerService;
    }

    private boolean isCPUAvaiable() {
        return osBean.getSystemCpuLoad() < devProperties.getMaxCpuUsage() && devProperties.getUsableCores() > activeRunningJobs;
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
        while (activeRunningJobs != 0 && antiStuck != 0) {
            TimeUnit.SECONDS.sleep(1);
            antiStuck--;
        }
    }

    @Override
    public void halt(int maxAllowedJobs) throws InterruptedException {
        halted = true;
        int antiStuck = 30;
        while (activeRunningJobs > maxAllowedJobs && antiStuck != 0) {
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
    @Async
    @Scheduled(fixedRate = 100)
    public void runJob() {

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
