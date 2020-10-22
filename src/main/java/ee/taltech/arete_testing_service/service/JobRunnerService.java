package ee.taltech.arete_testing_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete_testing_service.configuration.DevProperties;
import ee.taltech.arete_testing_service.domain.DefaultParameters;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete.java.response.arete.FileDTO;
import ee.taltech.arete.java.response.hodor_studenttester.HodorStudentTesterResponse;
import ee.taltech.arete_testing_service.service.arete.AreteConstructor;
import ee.taltech.arete_testing_service.service.docker.DockerService;
import ee.taltech.arete_testing_service.service.git.GitPullService;
import ee.taltech.arete_testing_service.service.hodor.HodorParser;
import ee.taltech.arete_testing_service.service.uva.UvaTestRunner;
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

        if (createDirs(submission)) return; // if error, done

        formatSlugs(submission);
        LOGGER.info("Running slugs {} for {}", submission.getSlugs(), submission.getUniid());
        String initialEmail = submission.getEmail();

        for (String slug : submission.getSlugs()) {

            if (createDirs(submission)) {
                deleteDirs(submission);
                continue;
            }

            rootProperties(submission);
            groupingFolderProperties(submission, slug);
            slugProperties(submission, slug);

            studentRootProperties(submission);
            studentGroupingFolderProperties(submission, slug);
            studentSlugProperties(submission, slug);

            modifyEmail(submission, initialEmail);

            readTesterFiles(submission, slug);
            readStudentFiles(submission, slug);

            try {
                invokeTestRunner(submission, slug);
            } catch (Exception e) {
                LOGGER.error("job {} has failed for user {} with exception: {}", slug, submission.getUniid(), e.getMessage());
                reportFailedSubmission(submission, e.getMessage());
            } finally {
                deleteDirs(submission);
            }
        }
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

    @SneakyThrows
    private void invokeTestRunner(Submission submission, String slug) {
        switch (submission.getTestingEnvironment()) {
            case DOCKER:
                String outputPath = dockerService.runDocker(submission, slug);
                reportSuccessfulSubmission(slug, submission, outputPath);
                LOGGER.info("DOCKER Job {} has been ran for user {}", slug, submission.getUniid());
                break;
            case UVA:
                String problemID = submission.getUvaConfiguration().getProblemID();
                if (problemID == null) {
                    problemID = slug;
                }
                problemID = problemID.split("-")[0].strip();

                String userID = submission.getUvaConfiguration().getUserID();

                AreteResponseDTO response = UvaTestRunner.fetchResult(userID, problemID);
                submission.getSystemExtra().add("noStyle");
                submission.getSystemExtra().add("noOverall");
                AreteConstructor.fillFromSubmission(slug, submission, response);
                reportSubmission(submission, response, response.getOutput(), slug, true, Optional.empty());
                break;
            case HACKERRANK:
                reportFailedSubmission(submission, "HACKERRANK integration has not yet been implemented");
                break;
            case LEETCODE:
                reportFailedSubmission(submission, "LEETCODE integration has not yet been implemented");
                break;
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

    private void deleteDirs(Submission submission) {
        try {
            FileUtils.deleteDirectory(new File(String.format("input_and_output/%s", submission.getHash())));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void formatSlugs(Submission submission) {
        rootProperties(submission); // load groupingFolders

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

    public void rootProperties(Submission submission) {
        if (!submission.getSystemExtra().contains("noOverride")) {
            try {
                String path = String.format("tests/%s/arete.json", submission.getCourse());
                DefaultParameters params = objectMapper.readValue(new File(path), DefaultParameters.class);
                params.overrideParameters(submission);
                LOGGER.info("Overrode default parameters: {}", params);
            } catch (Exception e) {
                LOGGER.info("Using default parameters: {}", e.getMessage());
            }
        }
    }

    public void groupingFolderProperties(Submission submission, String slug) {
        Optional<String> group = extractGroupFromSlug(slug);
        if (!submission.getSystemExtra().contains("noOverride") && group.isPresent()) {
            try {
                String path = String.format("tests/%s/%s/arete.json", submission.getCourse(), group.get());
                DefaultParameters params = objectMapper.readValue(new File(path), DefaultParameters.class);
                params.overrideParameters(submission);
                LOGGER.info("Overrode default parameters: {}", params);
            } catch (Exception e) {
                LOGGER.info("Using default parameters: {}", e.getMessage());
            }
        }
    }

    public void slugProperties(Submission submission, String slug) {
        if (!submission.getSystemExtra().contains("noOverride")) {
            try {
                String path = String.format("tests/%s/%s/arete.json", submission.getCourse(), slug);
                DefaultParameters params = objectMapper.readValue(new File(path), DefaultParameters.class);
                params.overrideParameters(submission);
                LOGGER.info("Overrode default parameters: {}", params);
            } catch (Exception e) {
                LOGGER.info("Using default parameters: {}", e.getMessage());
            }
        }
    }


    public void studentRootProperties(Submission submission) {
        if (!submission.getSystemExtra().contains("noOverride")) {
            try {
                String path = String.format("students/%s/%s/arete.json", submission.getUniid(), submission.getFolder());
                DefaultParameters params = objectMapper.readValue(new File(path), DefaultParameters.class);
                params.overrideParametersForStudent(submission);
                LOGGER.info("Overrode default parameters: {}", params);
            } catch (Exception e) {
                LOGGER.info("Using default parameters: {}", e.getMessage());
            }
        }
    }

    public void studentGroupingFolderProperties(Submission submission, String slug) {
        Optional<String> group = extractGroupFromSlug(slug);
        if (!submission.getSystemExtra().contains("noOverride") && group.isPresent()) {
            try {
                String path = String.format("students/%s/%s/%s/arete.json", submission.getUniid(), submission.getFolder(), group.get());
                DefaultParameters params = objectMapper.readValue(new File(path), DefaultParameters.class);
                params.overrideParametersForStudent(submission);
                LOGGER.info("Overrode default parameters: {}", params);
            } catch (Exception e) {
                LOGGER.info("Using default parameters: {}", e.getMessage());
            }
        }
    }


    public void studentSlugProperties(Submission submission, String slug) {
        if (!submission.getSystemExtra().contains("noOverride")) {
            try {
                String path = String.format("students/%s/%s/%s/arete.json", submission.getUniid(), submission.getFolder(), slug);
                DefaultParameters params = objectMapper.readValue(new File(path), DefaultParameters.class);
                params.overrideParametersForStudent(submission);
                LOGGER.info("Overrode default parameters: {}", params);
            } catch (Exception e) {
                LOGGER.info("Using default parameters: {}", e.getMessage());
            }
        }
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
                DefaultParameters params = objectMapper.readValue(new File(String.format("tests/%s/arete.json", submission.getCourse())), DefaultParameters.class);
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

            } catch (Exception e) {
                e.printStackTrace();
                priorityQueueService.go();
                LOGGER.error("Job execution failed for {} with message: {}", submission.getUniid(), e.getMessage());
                reportFailedSubmission(submission, e.getMessage());
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
                LOGGER.error("Job execution failed for {} with message: {}", submission.getUniid(), e.getMessage());
                reportFailedSubmission(submission, e.getMessage());
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
                LOGGER.error(e1.getMessage());
                if (jsonObject.has("output") && jsonObject.get("output") != null) {
                    areteResponse = AreteConstructor.failedSubmission(slug, submission, jsonObject.get("output").toString());
                } else {
                    areteResponse = AreteConstructor.failedSubmission(slug, submission, e1.getMessage());
                }
            }

            message = areteResponse.getOutput();

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            message = String.format("Error occurred when reading test results from docker created output.json.\nThis could be a result for invalid dockerExtra or other reason, that resulted in docker crashing.\n%s", e.getMessage());
            areteResponse = AreteConstructor.failedSubmission(slug, submission, e.getMessage()); // create failed submission instead
        }

        reportSubmission(submission, areteResponse, message, slug, html, Optional.of(outputPath));

    }

    public AreteResponseDTO getAreteResponse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, AreteResponseDTO.class);
    }

    private void reportFailedSubmission(Submission submission, String errorMessage) {
        String message = String.format("Testing failed with message: %s", errorMessage); // Sent to student
        AreteResponseDTO areteResponse;
        if (submission.getSlugs() == null) {
            areteResponse = AreteConstructor.failedSubmission("undefined", submission, message); // Sent to Moodle
        } else {
            areteResponse = AreteConstructor.failedSubmission(submission.getSlugs().stream().findFirst().orElse("undefined"), submission, message); // Sent to Moodle
        }
        if (submission.getSystemExtra().contains("integration_tests")) {
            LOGGER.error("FAILED WITH MESSAGE: {}", message);
        }

        reportSubmission(submission, areteResponse, message, "Failed submission", false, Optional.empty());
    }

    @SneakyThrows
    private void reportSubmission(Submission submission, AreteResponseDTO areteResponse, String message, String header, Boolean html, Optional<String> output) {

        if (submission.getSystemExtra().contains("integration_tests")) {
            reportService.sendTextToReturnUrl(submission.getReturnUrl(), objectMapper.writeValueAsString(areteResponse));
            LOGGER.info("INTEGRATION TEST: Reported to return url for {} with score {}%", submission.getUniid(), areteResponse.getTotalGrade());

            String integrationTestMail = System.getenv("INTEGRATION_TEST_MAIL");
            if (integrationTestMail != null) {
                reportService.sendTextMail(integrationTestMail, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission), header, html, output);
            }
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
                    if (!devProperties.getAgo().equals(devProperties.getDeveloper())) {
                        reportService.sendTextMail(devProperties.getDeveloper(), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission), header, html, output);
                    }
                } catch (Exception e) {
                    reportService.sendTextMail(devProperties.getAgo(), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission), header, html, Optional.empty());
                    if (!devProperties.getAgo().equals(devProperties.getDeveloper())) {
                        reportService.sendTextMail(devProperties.getDeveloper(), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission), header, html, Optional.empty());
                    }
                }
            }

        } catch (Exception e1) {
            LOGGER.error("Malformed mail: {}", e1.getMessage());
        }
    }


}
