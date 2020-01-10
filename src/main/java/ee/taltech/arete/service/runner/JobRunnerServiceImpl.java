package ee.taltech.arete.service.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.api.data.response.hodor_studenttester.hodorStudentTesterResponse;
import ee.taltech.arete.api.data.response.legacy.LegacyTestJobResult;
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
				if (!gitPullService.repositoryMaintenance(submission)) {
					reportFailedSubmission(submission, new RuntimeException(submission.getResult()));
					priorityQueueService.killThread(submission);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("Job execution failed for {} with message: {}", submission.getUniid(), e.getMessage());
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

			reportSuccessfulSubmission(slug, submission, output);

			try {
				new PrintWriter(output).close(); // clears output file
			} catch (Exception ignored) {
			}

		}

		priorityQueueService.killThread(submission);
	}

	private void reportSuccessfulSubmission(String slug, Submission submission, String output) {

		AreteResponse areteResponse; // Sent to Moodle
		String message; // Sent to student
		boolean html = false;

		try {
			String json = Files.readString(Paths.get(output), StandardCharsets.UTF_8);
			JSONObject jsonObject = new JSONObject(json);

			try {
				if ("hodor_studenttester".equals(jsonObject.get("type"))) {
					html = true;
					hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
					areteResponse = new AreteResponse(slug, submission, response);
				} else if ("hodor_legacy".equals(jsonObject.get("type"))) {
					LegacyTestJobResult response = objectMapper.readValue(json, LegacyTestJobResult.class);
					areteResponse = new AreteResponse(slug, submission, response);
				} else {
					areteResponse = new AreteResponse(slug, submission, "Unsupported tester type.");
				}
			} catch (Exception e1) {
				html = false;
				e1.printStackTrace();
				if (jsonObject.get("output") != null) {
					areteResponse = new AreteResponse(slug, submission, jsonObject.get("output").toString());
				} else {
					areteResponse = new AreteResponse(slug, submission, e1.getMessage());
				}
			}

			message = areteResponse.getOutput();

		} catch (Exception e) {

			e.printStackTrace();
			throw new UnexpectedTypeException(e.getMessage());
		}

		reportSubmission(submission, areteResponse, message, html);

	}

	private void reportFailedSubmission(Submission submission, Exception e) {
		String message = e.getMessage(); // Sent to student
		AreteResponse areteResponse;
		if (submission.getSlugs() == null) {
			areteResponse = new AreteResponse("undefined", submission, message); // Sent to Moodle
		} else {
			areteResponse = new AreteResponse(submission.getSlugs().stream().findFirst().orElse("undefined"), submission, message); // Sent to Moodle
		}

		reportSubmission(submission, areteResponse, message, false);
	}

	private void reportSubmission(Submission submission, AreteResponse areteResponse, String message, Boolean html) {
		try {
			reportService.sendTextToReturnUrl(submission.getReturnUrl(), areteResponse);
			LOGGER.info("Reported to return url");
		} catch (Exception e1) {
			LOGGER.error("Malformed returnUrl: {}", e1.getMessage());
		}

		if (!submission.getSystemExtra().contains("noMail")) {
			try {
				reportService.sendTextMail(submission.getUniid(), message, html);
				LOGGER.info("Reported to student mailbox");
			} catch (Exception e1) {
				LOGGER.error("Malformed mail: {}", e1.getMessage());
			}
		}
	}
}
