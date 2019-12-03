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

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;


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

		try {
			gitPullService.repositoryMaintenance(submission);
		} catch (Exception e) {
			LOGGER.error("Student didn't have new submissions: {}", e.getMessage());

			reportFailedSubmission(submission, e);

			priorityQueueService.killThread(submission);
			return;
		}

		LOGGER.info("Running slugs {} for {}", submission.getSlugs(), submission.getUniid());

		for (String slug : submission.getSlugs()) {
			String output;
			try {
				output = dockerService.runDocker(submission, slug);
				LOGGER.info("Job {} has been ran for user {}", slug, submission.getUniid());
			} catch (Exception e) {
				LOGGER.error("job {} has failed for user {} with exception: {}", slug, submission.getUniid(), e.getMessage());

				reportFailedSubmission(submission, e);
				continue;
			}

			reportSuccessfulSubmission(submission, output);

		}

		priorityQueueService.killThread(submission);
	}

	private void reportSuccessfulSubmission(Submission submission, String output) {
		try {
			reportService.sendToReturnUrl(submission.getReturnUrl(), output);
			LOGGER.info("Reported to return url");
		} catch (Exception e) {
			LOGGER.error("Malformed returnUrl: {}", e.getMessage());
		}

		List<String> list = Arrays.asList(submission.getSystemExtra());
		if (!list.contains("noMail")) {
			try {
				reportService.sendMail(submission.getUniid(), output);
				LOGGER.info("Reported to student mailbox");
			} catch (Exception e) {
				LOGGER.error("Malformed mail: {}", e.getMessage());
			}
		}

		try {
			new PrintWriter(output).close(); // clears output file
		} catch (Exception ignored) {
		}

	}

	private void reportFailedSubmission(Submission submission, Exception e) {
		String message = e.getMessage() + "\n\n\nHere are tester logs:\n\n" + submission.getResult().toString();
		try {
			reportService.sendTextToReturnUrl(submission.getReturnUrl(), message);
			LOGGER.info("Reported to url");
		} catch (Exception e1) {
			LOGGER.error("Malformed returnUrl: {}", e1.getMessage());
		}

		List<String> list = Arrays.asList(submission.getSystemExtra());
		if (!list.contains("noMail")) {
			try {
				reportService.sendTextMail(submission.getUniid(), message);
				LOGGER.info("Reported to student mailbox");
			} catch (Exception e1) {
				LOGGER.error("Malformed mail: {}", e1.getMessage());
			}
		}

	}
}
