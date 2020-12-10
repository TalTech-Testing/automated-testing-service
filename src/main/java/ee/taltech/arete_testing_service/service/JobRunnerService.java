package ee.taltech.arete_testing_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete.java.response.arete.FileDTO;
import ee.taltech.arete.java.response.hodor_studenttester.HodorStudentTesterResponse;
import ee.taltech.arete_testing_service.configuration.DevProperties;
import ee.taltech.arete_testing_service.domain.OverrideParameters;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.service.arete.AreteConstructor;
import ee.taltech.arete_testing_service.service.docker.DockerService;
import ee.taltech.arete_testing_service.service.git.GitPullService;
import ee.taltech.arete_testing_service.service.hodor.HodorParser;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
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

	@SneakyThrows
	public void runJob(Submission submission) {

		if (folderMaintenance(submission)) return; // if error, done

		formatSlugs(submission);
		LOGGER.info("Running slugs {} for {}", submission.getSlugs(), submission.getUniid());
		String initialEmail = submission.getEmail();

		for (String slug : submission.getSlugs()) {

			if (createDirs(submission, slug)) {
				continue;
			}

			Optional<OverrideParameters> testRoot = rootProperties(submission);
			Optional<OverrideParameters> testGroup = groupingFolderProperties(submission, slug);
			Optional<OverrideParameters> testSlug = slugProperties(submission, slug);

			Optional<OverrideParameters> studentRoot = studentRootProperties(submission);
			Optional<OverrideParameters> studentGroup = studentGroupingFolderProperties(submission, slug);
			Optional<OverrideParameters> studentSlug = studentSlugProperties(submission, slug);

			modifyEmail(submission, initialEmail);

			readTesterFiles(submission, slug);
			readStudentFiles(submission, slug);

			boolean[] changed = fillMissingValues(submission);

			runTests(submission, slug);

			revertAddedDefaults(submission, changed);

			studentSlug.ifPresent(x -> x.revert(submission));
			studentGroup.ifPresent(x -> x.revert(submission));
			studentRoot.ifPresent(x -> x.revert(submission));

			testSlug.ifPresent(x -> x.revert(submission));
			testGroup.ifPresent(x -> x.revert(submission));
			testRoot.ifPresent(x -> x.revert(submission));
		}
	}

	private void revertAddedDefaults(Submission submission, boolean[] changed) {
		if (changed[0]) {
			submission.setDockerContentRoot(null);
		}

		if (changed[1]) {
			submission.setDockerExtra(null);
		}

		if (changed[2]) {
			submission.setDockerTestRoot(null);
		}
	}

	private void runTests(Submission submission, String slug) {
		try {
			String outputPath = dockerService.runDocker(submission, slug);
			LOGGER.info("DOCKER Job {} has been ran for user {}", slug, submission.getUniid());
			reportSuccessfulSubmission(slug, submission, outputPath);
		} catch (Exception e) {
			LOGGER.error("job {} has failed for user {} with exception: {}", slug, submission.getUniid(), e.getMessage());
			reportFailedSubmission(submission, e.getMessage());
		}
	}

	private boolean[] fillMissingValues(Submission submission) {
		boolean[] modified = new boolean[]{false, false, false};

		if (submission.getDockerContentRoot() == null) {
			submission.setDockerContentRoot("/student");
			modified[0] = true;
		}

		if (submission.getDockerExtra() == null) {
			submission.setDockerExtra("");
			modified[1] = true;
		}

		if (submission.getDockerTestRoot() == null) {
			submission.setDockerTestRoot("/tester");
			modified[2] = true;
		}

		return modified;
	}

	private void readStudentFiles(Submission submission, String slug) {
		try {
			if (submission.getGitStudentRepo() != null && submission.getGitStudentRepo().contains("git") && submission.getSource() == null) {
				String student = String.format("students/%s/%s/%s", submission.getUniid(), submission.getFolder(), slug);

				FileUtils.listFiles(
						new File(student),
						new RegexFileFilter("^(.*?)"),
						DirectoryFileFilter.DIRECTORY
				).parallelStream().sequential()
						.forEach(x -> {
							try {
								submission.setSource(new ArrayList<>());
								submission.getSource()
										.add(FileDTO.builder()
												.contents(Files.readString(x.toPath(), StandardCharsets.UTF_8))
												.path(x.getPath())
												.build());
							} catch (IOException e) {
								submission.setSource(null);
							}
						});
			}
		} catch (Exception e) {
			submission.setSource(null);
		}
	}

	private void readTesterFiles(Submission submission, String slug) {
		try {
			if (submission.getGitTestRepo() != null && submission.getGitTestRepo().contains("git") && submission.getTestSource() == null) {
				String tester = String.format("tests/%s/%s", submission.getCourse(), slug);

				FileUtils.listFiles(
						new File(tester),
						new RegexFileFilter("^(.*?)"),
						DirectoryFileFilter.DIRECTORY
				).parallelStream().sequential()
						.forEach(x -> {
							try {
								submission.setTestSource(new ArrayList<>());
								submission.getTestSource()
										.add(FileDTO.builder()
												.contents(Files.readString(x.toPath(), StandardCharsets.UTF_8))
												.path(x.getPath())
												.build());
							} catch (IOException e) {
								submission.setTestSource(null);
							}
						});
			}
		} catch (Exception e) {
			submission.setTestSource(null);
		}
	}

	public void modifyEmail(Submission submission, String initialEmail) {
		submission.setEmail(initialEmail);

		if (!submission.getSystemExtra().contains("allowExternalMail")) {
			if (!submission.getEmail().matches(devProperties.getSchoolMailMatcher())) {
				submission.setEmail(submission.getUniid() + "@ttu.ee");
			}
		}
	}

	public void formatSlugs(Submission submission) {
		HashSet<String> formattedSlugs = new HashSet<>();

		for (String changed_file : submission.getSlugs()) {
			String potentialSlug = changed_file.split("[/\\\\]")[0];
			if (potentialSlug.matches(devProperties.getNameMatcher())) {
				if (submission.getGroupingFolders().contains(potentialSlug)) {
					try {
						String innerPotentialSlug = changed_file.split("[/\\\\]")[1];
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

	private boolean createDirs(Submission submission, String slug) {

		try {
			new File(toRealPath(String.format("input_and_output/%s/%s", submission.getHash(), slug))).mkdirs();
			createDirectory(toRealPath(String.format("input_and_output/%s/%s/tester", submission.getHash(), slug)));
			createDirectory(toRealPath(String.format("input_and_output/%s/%s/student", submission.getHash(), slug)));
			createDirectory(toRealPath(String.format("input_and_output/%s/%s/host", submission.getHash(), slug)));

			new File(String.format("input_and_output/%s/%s/host/input.json", submission.getHash(), slug)).createNewFile();

			new File(String.format("input_and_output/%s/%s/host/output.json", submission.getHash(), slug)).createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}

		return false;
	}

	public Optional<OverrideParameters> rootProperties(Submission submission) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				String path = String.format("tests/%s/arete.json", submission.getCourse());
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParameters(submission);
				LOGGER.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				LOGGER.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}

	public Optional<OverrideParameters> groupingFolderProperties(Submission submission, String slug) {
		Optional<String> group = extractGroupFromSlug(slug);
		if (!submission.getSystemExtra().contains("noOverride") && group.isPresent()) {
			try {
				String path = String.format("tests/%s/%s/arete.json", submission.getCourse(), group.get());
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParameters(submission);
				LOGGER.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				LOGGER.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}

	public Optional<OverrideParameters> slugProperties(Submission submission, String slug) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				String path = String.format("tests/%s/%s/arete.json", submission.getCourse(), slug);
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParameters(submission);
				LOGGER.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				LOGGER.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}


	public Optional<OverrideParameters> studentRootProperties(Submission submission) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				String path = String.format("students/%s/%s/arete.json", submission.getUniid(), submission.getFolder());
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParametersForStudent(submission);
				LOGGER.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				LOGGER.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}

	public Optional<OverrideParameters> studentGroupingFolderProperties(Submission submission, String slug) {
		Optional<String> group = extractGroupFromSlug(slug);
		if (!submission.getSystemExtra().contains("noOverride") && group.isPresent()) {
			try {
				String path = String.format("students/%s/%s/%s/arete.json", submission.getUniid(), submission.getFolder(), group.get());
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParametersForStudent(submission);
				LOGGER.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				LOGGER.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}


	public Optional<OverrideParameters> studentSlugProperties(Submission submission, String slug) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				String path = String.format("students/%s/%s/%s/arete.json", submission.getUniid(), submission.getFolder(), slug);
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParametersForStudent(submission);
				LOGGER.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				LOGGER.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}

	public Optional<String> extractGroupFromSlug(String slug) {
		String[] groups = slug.split("[/\\\\]");
		if (groups.length > 1) {
			return Optional.of(groups[0]);
		}
		return Optional.empty();
	}

	public void testingProperties(Submission submission) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				OverrideParameters params = objectMapper.readValue(new File(String.format("tests/%s/arete.json", submission.getCourse())), OverrideParameters.class);
				params.overrideParametersForTestValidation(submission);
				LOGGER.info("Overrode default parameters: {}", params);
			} catch (Exception e) {
				LOGGER.info("Using default parameters: {}", e.getMessage());
			}
		}
	}

	private boolean folderMaintenance(Submission submission) {
		if (submission.getGitTestRepo() != null) {
			try {
				String pathToTesterFolder = String.format("tests/%s/", submission.getCourse());
				String pathToTesterRepo = submission.getGitTestRepo();
				File f = new File(pathToTesterFolder);

				if (!f.exists()) {
					LOGGER.info("Checking for update for tester: {}", pathToTesterFolder);
					priorityQueueService.halt(1); // only allow this job.. then continue to pull tests

					if (gitPullService.pullOrClone(pathToTesterFolder, pathToTesterRepo, Optional.empty())) {
						priorityQueueService.go();
					} else {
						priorityQueueService.go();
						reportFailedSubmission(submission, "No test files");
						return true;
					}
				}

				rootProperties(submission); // preload initial configuration
			} catch (Exception e) {
				priorityQueueService.go();
				String message = "Job execution failed for " + submission.getUniid() + " with message: " + e.getMessage();
				LOGGER.error(message);
				reportFailedSubmission(submission, message);
				return true;
			}
		}

		if (submission.getGitStudentRepo() != null) {
			try {

				if (!gitPullService.repositoryMaintenance(submission)) {
					reportFailedSubmission(submission, submission.getResult());
					return true;
				}

			} catch (Exception e) {
				String message = "Job execution failed for " + submission.getUniid() + " with message: " + e.getMessage();
				LOGGER.error(message);
				reportFailedSubmission(submission, message);
				return true;
			}
		}
		return false;
	}

	public void reportSuccessfulSubmission(String slug, Submission submission, String outputPath) {

		AreteResponseDTO areteResponse; // Sent to Moodle
		String message; // Sent to student
		boolean html = false;

		try {
			String json = Files.readString(Paths.get(outputPath + "/output.json"), StandardCharsets.UTF_8);
			JSONObject jsonObject = new JSONObject(json);

			try {
				if ("hodor_studenttester".equals(jsonObject.get("type"))) {
					html = true;
					HodorStudentTesterResponse response = objectMapper.readValue(json, HodorStudentTesterResponse.class);
					areteResponse = HodorParser.parse(response);
					AreteConstructor.fillFromSubmission(slug, submission, areteResponse);

				} else if ("arete".equals(jsonObject.get("type"))) {
					html = true;
					areteResponse = getAreteResponse(json);
					AreteConstructor.fillFromSubmission(slug, submission, areteResponse);

				} else {
					areteResponse = AreteConstructor.failedSubmission(slug, submission, "Unsupported tester type.");
				}
			} catch (Exception e1) {
				html = false;
				LOGGER.error("Failed constructing areteResponse: {}", e1.getMessage());
				if (jsonObject.has("output") && jsonObject.get("output") != null) {
					areteResponse = AreteConstructor.failedSubmission(slug, submission, jsonObject.get("output").toString());
				} else {
					message = "Error occurred when reading test results from TestRunner created output. This is most likely due to invalid runtime configuration, that resulted in tester not giving a result.";
					areteResponse = AreteConstructor.failedSubmission(slug, submission, message);
				}
			}

			message = areteResponse.getOutput();

		} catch (Exception e) {
			LOGGER.error("Generating a failed response: {}", e.getMessage());
			message = "Error occurred when reading test results from TestRunner created output. This is most likely due to invalid runtime configuration, that resulted in tester not giving a result.";
			areteResponse = AreteConstructor.failedSubmission(slug, submission, message);
		}

		reportSubmission(submission, areteResponse, message, slug, html, Optional.of(outputPath));

	}

	public AreteResponseDTO getAreteResponse(String json) throws JsonProcessingException {
		AreteResponseDTO responseDTO = objectMapper.readValue(json, AreteResponseDTO.class);
		responseDTO.setType("arete");
		responseDTO.setVersion("2.1");
		return responseDTO;
	}

	private void reportFailedSubmission(Submission submission, String errorMessage) {
		String message = String.format("Testing failed with message: %s", errorMessage);
		AreteResponseDTO areteResponse;
		if (submission.getSlugs() == null) {
			areteResponse = AreteConstructor.failedSubmission("undefined", submission, message);
		} else {
			areteResponse = AreteConstructor.failedSubmission(submission.getSlugs().stream().findFirst().orElse("undefined"), submission, message);
		}
		if (submission.getSystemExtra().contains("integration_tests")) {
			LOGGER.error("FAILED WITH MESSAGE: {}", message);
		}

		reportSubmission(submission, areteResponse, message, "Failed submission", false, Optional.empty());
	}

	@SneakyThrows
	private void reportSubmission(Submission submission, AreteResponseDTO areteResponse, String message, String header, Boolean html, Optional<String> output) {

		areteResponse.setConsoleOutputs("<CHARON_DEPLOY_PLS>");
		String areteJson = objectMapper.writeValueAsString(areteResponse);
		areteJson = areteJson.replaceAll("['\"]<CHARON_DEPLOY_PLS>['\"]", "[]");

		if (submission.getSystemExtra().contains("integration_tests")) {
			reportService.sendTextToReturnUrl(submission.getReturnUrl(), areteJson);
			LOGGER.info("INTEGRATION TEST: Reported to return url for {} with score {}%", submission.getUniid(), areteResponse.getTotalGrade());

			String integrationTestMail = System.getenv("INTEGRATION_TEST_MAIL");
			if (integrationTestMail != null) {
				reportService.sendTextMail(integrationTestMail, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission), header, html, output);
			}
			return;
		}

		try {
			if (submission.getReturnUrl() != null) {
				reportService.sendTextToReturnUrl(submission.getReturnUrl(), areteJson);
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

			reportService.sendTextToReturnUrl(devProperties.getAreteBackend(), areteJson);
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
				String submissionString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission);
				try {
					reportService.sendTextMail(devProperties.getAgo(), submissionString, header, html, output);
					if (!devProperties.getAgo().equals(devProperties.getDeveloper())) {
						reportService.sendTextMail(devProperties.getDeveloper(), submissionString, header, html, output);
					}
				} catch (Exception e) {
					reportService.sendTextMail(devProperties.getAgo(), submissionString, header, html, Optional.empty());
					if (!devProperties.getAgo().equals(devProperties.getDeveloper())) {
						reportService.sendTextMail(devProperties.getDeveloper(), submissionString, header, html, Optional.empty());
					}
				}
			}

		} catch (Exception e1) {
			LOGGER.error("Malformed mail: {}", e1.getMessage());
		}
	}
}
