package ee.taltech.arete.service.runner;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.RequestFormatException;
import ee.taltech.arete.service.docker.DockerService;
import ee.taltech.arete.service.git.GitPullService;
import ee.taltech.arete.service.response.ReportService;
import ee.taltech.arete.service.queue.PriorityQueueService;
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


		for (String slug : submission.getSlugs()) {
			dockerService.runDocker(submission, slug);
		}
		LOGGER.info("Job {} has been ran", submission);

		priorityQueueService.killThread();

		try {
			reportService.sendToReturnUrl(submission);
		} catch (Exception e) {
			throw new RequestFormatException("Malformed returnUrl");
		}

		try {
			reportService.sendMail(submission);
		} catch (Exception e) {
			throw new RequestFormatException("Malformed mail");
		}

	}
}