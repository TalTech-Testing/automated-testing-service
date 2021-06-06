package ee.taltech.arete_testing_service.service;

import com.sun.management.OperatingSystemMXBean;
import ee.taltech.arete_testing_service.configuration.ServerConfiguration;
import ee.taltech.arete_testing_service.domain.Submission;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class PriorityQueueService {

    private static final CopyOnWriteArrayList<Submission> activeSubmissions = new CopyOnWriteArrayList<>();
    private static Boolean halted = true;
    private static Integer jobsRan = 0;
    private static Integer stuckQueue = 3000; // just some protection against stuck queue
    private final OperatingSystemMXBean osBean = getOsBean();
    private final Logger logger;
    private final ServerConfiguration serverConfiguration;
    private final JobRunnerService jobRunnerService;
    private final BlockingQueue<Submission> submissionPriorityQueue = emptyQueue();

    @SneakyThrows
    public static void halt() {
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

    private BlockingQueue<Submission> emptyQueue() {
        return new PriorityBlockingQueue<>(10000, Comparator
                .comparingInt(Submission::getPriority)
                .reversed()
                .thenComparing(Submission::getReceivedTimestamp));
    }

    private OperatingSystemMXBean getOsBean() {
        return ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    }

    public Integer getJobsRan() {
        return jobsRan;
    }

    @SneakyThrows
    @Scheduled(cron = "0 0 0 1 1/1 *")
    public void monthlyCleanUp() {
        logger.info("Running monthly cleanup. Deleting contents of students folder");

        halt();
        FileUtils.cleanDirectory(new File("students"));
        go();
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

        try {
            TimeUnit.SECONDS.sleep(10); // keep files for a little bit so mail can send them and prevent spam pushing
            FileUtils.deleteDirectory(new File(String.format("input_and_output/%s", submission.getHash())));
        } catch (Exception e) {
            logger.error("Failed deleting directory after killing thread: {}", e.getMessage());
        }

        activeSubmissions.remove(submission);
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

        if (!halted && getQueueSize() > 0 && isCPUAvaiable()) {

            Submission job;
            try {
                job = submissionPriorityQueue.poll();
            } catch (Exception e) {
                return;
            }

            if (job == null) {
                return;
            }

            if (job.getUniid() != null && activeSubmissions.stream().anyMatch(o -> o.getUniid().equals(job.getUniid()))) {
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
                e.printStackTrace();
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
        return osBean.getSystemCpuLoad() < serverConfiguration.getMaxCpuUsage() && serverConfiguration.getParallelJobs() > activeSubmissions.size();
    }

    @SneakyThrows
    public void enqueue(Submission submission) {
        submissionPriorityQueue.add(submission);
    }
}
