package ee.taltech.arete_testing_service.service;

import ee.taltech.arete_testing_service.domain.OverrideParametersCollection;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.service.docker.DockerService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class JobRunnerService {

    private final Logger logger;
    private final SubmissionPropertyService submissionPropertyService;
    private final DockerService dockerService;
    private final FolderManagementService folderManagementService;
    private final ReportService reportService;


    @SneakyThrows
    public void runJob(Submission submission) {

        if (folderManagementService.folderMaintenance(submission)) return; // if error, done

        submissionPropertyService.formatSlugs(submission);
        logger.info("Running slugs {} for {}", submission.getSlugs(), submission.getUniid());
        String initialEmail = submission.getEmail();

        for (String slug : submission.getSlugs()) {

            if (folderManagementService.createDirsForSubmission(submission, slug)) {
                continue;
            }

            OverrideParametersCollection coll = submissionPropertyService.update(submission, slug, initialEmail);

            runTests(submission, slug);

            submissionPropertyService.revert(submission, coll);
        }
    }

    private void runTests(Submission submission, String slug) {
        try {
            String outputPath = dockerService.runDocker(submission, slug);
            logger.info("DOCKER Job {} has been ran for user {}", slug, submission.getUniid());
            reportService.reportSuccessfulSubmission(slug, submission, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("job {} has failed for user {} with exception: {}", slug, submission.getUniid(), e.getMessage());
            reportService.reportFailedSubmission(submission, e.getMessage());
        }
    }

}
