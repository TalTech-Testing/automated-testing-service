package ee.taltech.arete.service.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.api.data.response.hodor_studenttester.hodorStudentTesterResponse;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.docker.DockerService;
import ee.taltech.arete.service.git.GitPullService;
import ee.taltech.arete.service.queue.PriorityQueueService;
import ee.taltech.arete.service.response.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.validation.UnexpectedTypeException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


@Service
public class JobRunnerServiceImpl implements JobRunnerService {

	private static Logger LOGGER = LoggerFactory.getLogger(JobRunnerService.class);

	@Autowired
	private ObjectMapper objectMapper;

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

		if (submission.getSource() == null) {
			try {
				gitPullService.repositoryMaintenance(submission);
			} catch (Exception e) {
				LOGGER.error("Student didn't have new submissions: {}", e.getMessage());

				reportFailedSubmission(submission, e);

				priorityQueueService.killThread(submission);
				return;
			}
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

			try {
				new PrintWriter(output).close(); // clears output file
			} catch (Exception ignored) {
			}

		}

		priorityQueueService.killThread(submission);
	}

	private void reportSuccessfulSubmission(Submission submission, String output) {

		AreteResponse areteResponse; // Sent to Moodle
		String message; // Sent to student

		try {
			String json = Files.readString(Paths.get(output), StandardCharsets.UTF_8);
			JSONObject jsonObject = new JSONObject(json);
			if ("hodor_studenttester".equals(jsonObject.get("type"))) {
				hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
				areteResponse = new AreteResponse(submission, response);
			} else {
				areteResponse = new AreteResponse(submission, "Unsupported tester type.");
			}
			message = areteResponse.getOutput();

		} catch (Exception e) {
			throw new UnexpectedTypeException(e.getMessage());
		}

		reportSubmission(submission, areteResponse, message);

	}

	private void reportFailedSubmission(Submission submission, Exception e) {
		String message = e.getMessage(); // Sent to student
		AreteResponse areteResponse = new AreteResponse(submission, message); // Sent to Moodle

		reportSubmission(submission, areteResponse, message);
	}

	private void reportSubmission(Submission submission, AreteResponse areteResponse, String message) {
		try {
			reportService.sendTextToReturnUrl(submission.getReturnUrl(), areteResponse);
			LOGGER.info("Reported to return url");
		} catch (Exception e1) {
			LOGGER.error("Malformed returnUrl: {}", e1.getMessage());
		}

		if (!Arrays.asList(submission.getSystemExtra()).contains("noMail")) {
			try {
				reportService.sendTextMail(submission.getUniid(), message);
				LOGGER.info("Reported to student mailbox");
			} catch (Exception e1) {
				LOGGER.error("Malformed mail: {}", e1.getMessage());
			}
		}
	}
}
