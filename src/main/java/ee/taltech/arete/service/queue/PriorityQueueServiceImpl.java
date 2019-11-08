package ee.taltech.arete.service.queue;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.runner.JobRunnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;

@Service
public class PriorityQueueServiceImpl implements PriorityQueueService {

    @Autowired
    private JobRunnerService jobRunnerService;

    private static final Integer MAX_JOBS = 3;
    private Integer runningJobs = 0;

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
    public Integer getQueueSize() {
        return submissionPriorityQueue.size();
    }

    @Override
    public Optional<Submission> runJob() {
        if (getQueueSize() == 0) {
            return Optional.empty();
        } else {
            if (runningJobs < MAX_JOBS) {

                runningJobs++;
	            Submission job = submissionPriorityQueue.remove();

	            Thread thread = new Thread(() -> {
		            jobRunnerService.runJob(job);
	            });

	            thread.start();

                LOGGER.info("Running job: {}", job);
                return Optional.of(job);
            }

            LOGGER.info("Queue is full. Cant run job. Job will be ran in the future.");
            return Optional.empty();

        }
    }
}
