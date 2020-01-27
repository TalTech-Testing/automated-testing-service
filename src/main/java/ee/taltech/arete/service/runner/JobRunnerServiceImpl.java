package ee.taltech.arete.service.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.api.data.response.arete.ConsoleOutput;
import ee.taltech.arete.api.data.response.hodor_studenttester.hodorStudentTesterResponse;
import ee.taltech.arete.api.data.response.legacy.LegacyTestJobResult;
import ee.taltech.arete.configuration.DevProperties;
import ee.taltech.arete.domain.DefaultParameters;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.docker.DockerService;
import ee.taltech.arete.service.git.GitPullService;
import ee.taltech.arete.service.queue.PriorityQueueService;
import ee.taltech.arete.service.response.ReportService;
import ee.taltech.arete.service.submission.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;


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

    @Autowired
    SubmissionService submissionService;

    @Autowired
    DevProperties devProperties;

    @Override
    public void runJob(Submission submission) {

        if (folderMaintenance(submission)) return; // if error, done

        LOGGER.info("Running slugs {} for {}", submission.getSlugs(), submission.getUniid());
        if (!submission.getSystemExtra().contains("noOverride")) {
            try {
                objectMapper
                        .readValue(new File(String.format("tests/%s/arete.json", submission.getCourse())), DefaultParameters.class)
                        .overrideDefaults(submission);
            } catch (Exception ignored) {
            }
        }


        for (String slug : submission.getSlugs()) {
            if (!submission.getSystemExtra().contains("noOverride")) {
                try {
                    objectMapper
                            .readValue(new File(String.format("tests/%s/%s/arete.json", submission.getCourse(), slug)), DefaultParameters.class)
                            .overrideDefaults(submission);
                    LOGGER.debug("Overrode default parameters");
                } catch (Exception e) {
                    LOGGER.debug("Using default parameters");
                }
            }

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
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }

            try {
                new PrintWriter(String.format("input_and_output/%s/host/input.json", submission.getThread())).close(); // clears input file
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
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

                } else if ("arete".equals(jsonObject.get("type"))) {
                    html = true;
                    areteResponse = objectMapper.readValue(json, AreteResponse.class);

                    if (!submission.getSystemExtra().contains("noStd")) {
                        areteResponse.getConsoleOutputs().add(new ConsoleOutput(submission.getResult()));
                    }

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

        reportSubmission(submission, areteResponse, message, slug, html);

    }

    private void reportFailedSubmission(Submission submission, Exception e) {
        String message = String.format("Testing failed with message: %s", e.getMessage()); // Sent to student
        AreteResponse areteResponse;
        if (submission.getSlugs() == null) {
            areteResponse = new AreteResponse("undefined", submission, message); // Sent to Moodle
        } else {
            areteResponse = new AreteResponse(submission.getSlugs().stream().findFirst().orElse("undefined"), submission, message); // Sent to Moodle
        }

        reportSubmission(submission, areteResponse, message, "Failed submission", false);
    }

    private void reportSubmission(Submission submission, AreteResponse areteResponse, String message, String header, Boolean html) {
        try {
            reportService.sendTextToReturnUrl(submission.getReturnUrl(), areteResponse);
            LOGGER.info("Reported to return url for {} with score {}%", submission.getUniid(), areteResponse.getTotalGrade());
        } catch (Exception e1) {
            LOGGER.error("Malformed returnUrl: {}", e1.getMessage());
        }

        if (!submission.getSystemExtra().contains("noMail")) {
            try {
                reportService.sendTextMail(submission.getUniid(), message, header, html);
                LOGGER.info("Reported to student mailbox");
            } catch (Exception e1) {
                LOGGER.error("Malformed mail: {}", e1.getMessage());
            }
        }

        if (!System.getenv().containsKey("GITLAB_PASSWORD") && submissionService.isDebug()) {

            try {
                if (areteResponse.getFailed()) {
                    reportService.sendTextMail(devProperties.getAgo(), String.format("Message: %s\n\n\nDocker log: %s", message, submission.getResult()), header, html);
                    reportService.sendTextMail(devProperties.getDeveloper(), String.format("Message: %s\n\n\nDocker log: %s", message, submission.getResult()), header, html);
                } else {
                    reportService.sendTextMail(devProperties.getDeveloper(), message, header, html);
                }

            } catch (Exception e1) {
                LOGGER.error("Malformed mail: {}", e1.getMessage());
            }
        }

    }
}
