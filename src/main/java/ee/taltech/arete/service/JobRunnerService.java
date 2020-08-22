package ee.taltech.arete.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.api.data.response.hodor_studenttester.hodorStudentTesterResponse;
import ee.taltech.arete.api.data.response.legacy.LegacyTestJobResult;
import ee.taltech.arete.configuration.DevProperties;
import ee.taltech.arete.domain.DefaultParameters;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.docker.DockerService;
import ee.taltech.arete.service.git.GitPullService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.h2.store.fs.FileUtils.createDirectory;
import static org.h2.store.fs.FileUtils.toRealPath;


@Service
public class JobRunnerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerService.class);
	private final PriorityQueueService priorityQueueService;
	private final DockerService dockerService;
	private final GitPullService gitPullService;
	private final ReportService reportService;
	private final DevProperties devProperties;
	private final ObjectMapper objectMapper;

	public JobRunnerService(PriorityQueueService priorityQueueService, DockerService dockerService, GitPullService gitPullService, ReportService reportService, DevProperties devProperties, ObjectMapper objectMapper) {
		this.priorityQueueService = priorityQueueService;
		this.dockerService = dockerService;
		this.gitPullService = gitPullService;
		this.reportService = reportService;
		this.devProperties = devProperties;
		this.objectMapper = objectMapper;
	}

	public List<String> runJob(Submission submission) {

		ArrayList<String> outputPaths = new ArrayList<>();

		if (folderMaintenance(submission)) return outputPaths; // if error, done

		if (createDirs(submission)) return outputPaths; // if error, done

		formatSlugs(submission);

		LOGGER.info("Running slugs {} for {}", submission.getSlugs(), submission.getUniid());

		for (String slug : submission.getSlugs()) {

			rootProperties(submission);

			slugProperties(submission, slug);

			String outputPath;

			try {
				outputPath = dockerService.runDocker(submission, slug);
				LOGGER.info("Job {} has been ran for user {}", slug, submission.getUniid());

			} catch (Exception e) {
				LOGGER.error("job {} has failed for user {} with exception: {}", slug, submission.getUniid(), e.getMessage());

				reportFailedSubmission(submission, e);
				continue;
			}

			reportSuccessfulSubmission(slug, submission, outputPath);

			outputPaths.add(outputPath);

		}
		return outputPaths;
	}

	public void formatSlugs(Submission submission) {
		rootProperties(submission); // load groupingFolders

		HashSet<String> formattedSlugs = new HashSet<>();

		for (String changed_file : submission.getSlugs()) {
			String potentialSlug = changed_file.split("/")[0];
			if (potentialSlug.matches(devProperties.getNameMatcher())) {
				if (submission.getGroupingFolders().contains(potentialSlug)) {
					try {
						String innerPotentialSlug = changed_file.split("/")[1];
						if (innerPotentialSlug.matches(devProperties.getNameMatcher())) {
							formattedSlugs.add(potentialSlug + "/" + innerPotentialSlug);
						}
					} catch (Exception ignored) {
					}
				} else {
					formattedSlugs.add(potentialSlug);
				}
			}
		}

		submission.setSlugs(formattedSlugs);
	}

	private boolean createDirs(Submission submission) {

		try {
			createDirectory(toRealPath(String.format("input_and_output/%s", submission.getHash())));
			createDirectory(toRealPath(String.format("input_and_output/%s/tester", submission.getHash())));
			createDirectory(toRealPath(String.format("input_and_output/%s/student", submission.getHash())));
			createDirectory(toRealPath(String.format("input_and_output/%s/host", submission.getHash())));

			new File(String.format("input_and_output/%s/host/input.json", submission.getHash())).createNewFile();

			new File(String.format("input_and_output/%s/host/output.json", submission.getHash())).createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}

		return false;
	}

	public void slugProperties(Submission submission, String slug) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				DefaultParameters params = objectMapper.readValue(new File(String.format("tests/%s/%s/arete.json", submission.getCourse(), slug)), DefaultParameters.class);
				params.overrideParametersForStudentValidation(submission);
				LOGGER.info("Overrode default parameters: {}", params);
			} catch (Exception e) {
				LOGGER.info("Using default parameters: {}", e.getMessage());
			}
		}
	}


	public void rootProperties(Submission submission) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				DefaultParameters params = objectMapper.readValue(new File(String.format("tests/%s/arete.json", submission.getCourse())), DefaultParameters.class);
				params.overrideParametersForStudentValidation(submission);
				LOGGER.info("Overrode default parameters: {}", params);
			} catch (Exception e) {
				LOGGER.info("Using default parameters: {}", e.getMessage());
			}
		}
	}

	public void testingProperties(Submission submission) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				DefaultParameters params = objectMapper.readValue(new File(String.format("tests/%s/arete.json", submission.getCourse())), DefaultParameters.class);
				params.overrideParametersForTestValidation(submission);
				LOGGER.info("Overrode default parameters: {}", params);
			} catch (Exception e) {
				LOGGER.info("Using default parameters: {}", e.getMessage());
			}
		}
	}

	private boolean folderMaintenance(Submission submission) {
		if (submission.getGitTestSource() != null) {
			try {
				String pathToTesterFolder = String.format("tests/%s/", submission.getCourse());
				String pathToTesterRepo = submission.getGitTestSource();
				File f = new File(pathToTesterFolder);

				if (!f.exists()) {
					LOGGER.info("Checking for update for tester: {}", pathToTesterFolder);
					priorityQueueService.halt(1); // only allow this job.. then continue to pull tests

					if (gitPullService.pullOrClone(pathToTesterFolder, pathToTesterRepo, Optional.empty())) {
						priorityQueueService.go();
					} else {
						priorityQueueService.go();
						reportFailedSubmission(submission, new RuntimeException("No test files"));
						return true;
					}
				}

			} catch (Exception e) {
				priorityQueueService.go();
				LOGGER.error("Job execution failed for {} with message: {}", submission.getUniid(), e.getMessage());
				reportFailedSubmission(submission, e);
				return true;
			}
		}

		if (submission.getGitStudentRepo() != null) {
			try {

				if (!gitPullService.repositoryMaintenance(submission)) {
					reportFailedSubmission(submission, new RuntimeException(submission.getResult()));
					return true;
				}

			} catch (Exception e) {
				LOGGER.error("Job execution failed for {} with message: {}", submission.getUniid(), e.getMessage());
				reportFailedSubmission(submission, e);
				return true;
			}
		}
		return false;
	}

	public void reportSuccessfulSubmission(String slug, Submission submission, String output) {

		AreteResponse areteResponse; // Sent to Moodle
		String message; // Sent to student
		boolean html = false;

		try {
			String json = Files.readString(Paths.get(output + "/output.json"), StandardCharsets.UTF_8);
			JSONObject jsonObject = new JSONObject(json);

			try {
				if ("hodor_studenttester".equals(jsonObject.get("type"))) {
					html = true;
					hodorStudentTesterResponse response = objectMapper.readValue(json, hodorStudentTesterResponse.class);
					areteResponse = new AreteResponse(slug, submission, response);

				} else if ("arete".equals(jsonObject.get("type"))) {
					html = true;
					areteResponse = getAreteResponse(slug, submission, json);

				} else if ("hodor_legacy".equals(jsonObject.get("type"))) {
					LegacyTestJobResult response = objectMapper.readValue(json, LegacyTestJobResult.class);
					areteResponse = new AreteResponse(slug, submission, response);
				} else {
					areteResponse = new AreteResponse(slug, submission, "Unsupported tester type.");
				}
			} catch (Exception e1) {
				html = false;
				LOGGER.error(e1.getMessage());
				if (jsonObject.has("output") && jsonObject.get("output") != null) {
					areteResponse = new AreteResponse(slug, submission, jsonObject.get("output").toString());
				} else {
					areteResponse = new AreteResponse(slug, submission, e1.getMessage());
				}
			}

			message = areteResponse.getOutput();

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			message = String.format("Error occurred when reading test results from docker created output.json.\nThis could be a result for invalid dockerExtra or other reason, that resulted in docker crashing.\n%s", e.getMessage());
			areteResponse = new AreteResponse(slug, submission, e.getMessage()); // create failed submission instead
		}

		reportSubmission(submission, areteResponse, message, slug, html, Optional.of(output));

	}

	public AreteResponse getAreteResponse(String slug, Submission submission, String json) throws JsonProcessingException {
		AreteResponse areteResponse = objectMapper.readValue(json, AreteResponse.class);

		if (submission.getSystemExtra().contains("noFiles")) {
			areteResponse.setFiles(new ArrayList<>());
			areteResponse.setTestFiles(new ArrayList<>());
		}

		if (submission.getSystemExtra().contains("noTesterFiles")) {
			areteResponse.setTestFiles(new ArrayList<>());
		}

		if (submission.getSystemExtra().contains("noStudentFiles")) {
			areteResponse.setFiles(new ArrayList<>());
		}

		if (areteResponse.getConsoleOutputs() == null || submission.getSystemExtra().contains("noStd")) {
			areteResponse.setConsoleOutputs(new ArrayList<>());
		}

		areteResponse.fillFromSubmission(slug, submission);

		if (areteResponse.getEmail() == null) {
			submission.setEmail(submission.getUniid() + "@ttu.ee");
		}

		if (areteResponse.getDockerExtra() == null) {
			areteResponse.setDockerExtra(submission.getDockerExtra());
		}

		if (areteResponse.getFailed() == null) {
			areteResponse.setFailed(false);
		}
		areteResponse.setOutput(areteResponse.constructOutput(submission));
		return areteResponse;
	}

	private void reportFailedSubmission(Submission submission, Exception e) {
		String message = String.format("Testing failed with message: %s", e.getMessage()); // Sent to student
		AreteResponse areteResponse;
		if (submission.getSlugs() == null) {
			areteResponse = new AreteResponse("undefined", submission, message); // Sent to Moodle
		} else {
			areteResponse = new AreteResponse(submission.getSlugs().stream().findFirst().orElse("undefined"), submission, message); // Sent to Moodle
		}

		reportSubmission(submission, areteResponse, message, "Failed submission", false, Optional.empty());
	}

	@SneakyThrows
	private void reportSubmission(Submission submission, AreteResponse areteResponse, String message, String header, Boolean html, Optional<String> output) {

		if (submission.getSystemExtra().contains("integration_tests")) {
			reportService.sendTextToReturnUrl(submission.getReturnUrl(), objectMapper.writeValueAsString(areteResponse));
			LOGGER.info("INTEGRATION TEST: Reported to return url for {} with score {}%", submission.getUniid(), areteResponse.getTotalGrade());
			return;
		}

		try {
			if (submission.getReturnUrl() != null) {
				reportService.sendTextToReturnUrl(submission.getReturnUrl(), objectMapper.writeValueAsString(areteResponse));
				LOGGER.info("Reported to return url for {} with score {}%", submission.getUniid(), areteResponse.getTotalGrade());
			}
		} catch (Exception e1) {
			LOGGER.error("Malformed returnUrl: {}", e1.getMessage());
		}

		try {

			if (submission.getSystemExtra().contains("anonymous")) {
				areteResponse.setReturnExtra(null);
			}

			JSONObject extra = new JSONObject();
			extra.put("used_extra", areteResponse.getReturnExtra());
			extra.put("shared_secret", System.getenv().getOrDefault("SHARED_SECRET", "Please make sure that shared_secret is set up properly"));
			areteResponse.setReturnExtra(new ObjectMapper().readTree(extra.toString()));

			reportService.sendTextToReturnUrl(devProperties.getAreteBackend(), objectMapper.writeValueAsString(areteResponse));
			LOGGER.info("Reported to backend");
		} catch (Exception e1) {
			LOGGER.error("Failed to report to backend with message: {}", e1.getMessage());
		}

		if (!submission.getSystemExtra().contains("noMail")) {
			try {
				reportService.sendTextMail(submission.getEmail(), message, header, html, output);
				LOGGER.info("Reported to {} mailbox", submission.getEmail());
			} catch (Exception e1) {
				LOGGER.error("Malformed mail: {}", e1.getMessage());
				areteResponse.setFailed(true);
				submission.setResult(submission.getResult() + "\n\n\n" + e1.getMessage());
			}
		}

		try {
			if (areteResponse.getFailed()) {
				try {
					reportService.sendTextMail(devProperties.getAgo(), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission), header, html, output);
					reportService.sendTextMail(devProperties.getDeveloper(), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission), header, html, output);
				} catch (Exception e) {
					reportService.sendTextMail(devProperties.getAgo(), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission), header, html, Optional.empty());
					reportService.sendTextMail(devProperties.getDeveloper(), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission), header, html, Optional.empty());
				}
			} else {
				reportService.sendTextMail(devProperties.getDeveloper(), message, header, html, output);
			}

		} catch (Exception e1) {
			LOGGER.error("Malformed mail: {}", e1.getMessage());
		}
	}


}
