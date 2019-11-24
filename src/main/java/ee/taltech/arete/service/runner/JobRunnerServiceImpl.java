package ee.taltech.arete.service.runner;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.docker.DockerService;
import ee.taltech.arete.service.git.GitPullService;
import ee.taltech.arete.service.queue.PriorityQueueService;
import ee.taltech.arete.service.response.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class JobRunnerServiceImpl implements JobRunnerService {

	private static Logger LOGGER = LoggerFactory.getLogger(JobRunnerService.class);

	@Autowired
	PriorityQueueService priorityQueueService;

	@Autowired
	DockerService dockerService;

	@Autowired
	GitPullService gitPullService;

	@Autowired
	ReportService reportService;

	@Override
	public void runJob(Submission submission) {
		gitPullService.repositoryMaintenance(submission);

		LOGGER.info("Running slugs {} for {}", submission.getSlugs(), submission.getUniid());

		if (submission.getSlugs() == null) {
			return;
		}

		for (String slug : submission.getSlugs()) {

			String output = dockerService.runDocker(submission, slug);
			LOGGER.info("Job {} has been ran for user {}", slug, submission.getUniid());

			try {
				reportService.sendToReturnUrl(submission, output);
				LOGGER.error("Reported to return url");
			} catch (Exception e) {
				LOGGER.error("Malformed returnUrl");
			}

			try {
				reportService.sendMail(submission, output);
				LOGGER.error("Reported to student mailbox");
			} catch (Exception e) {
				LOGGER.error("Malformed mail");
			}

		}

		priorityQueueService.killThread();
	}
}