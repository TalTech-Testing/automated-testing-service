package ee.taltech.arete_testing_service.service;

import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.service.git.GitPullService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

import static org.h2.store.fs.FileUtils.createDirectory;
import static org.h2.store.fs.FileUtils.toRealPath;

@Service
@AllArgsConstructor
public class FolderManagementService {

	private final Logger logger;
	private final ReportService reportService;
	private final SubmissionPropertyService submissionPropertyService;
	private final GitPullService gitPullService;


	public boolean createDirsForSubmission(Submission submission, String slug) {

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

	public boolean folderMaintenance(Submission submission) {
		if (submission.getGitTestRepo() != null &&
				!submission.getSystemExtra().contains("skipCopyingTests") &&
				!submission.getSystemExtra().contains("skipCopying")) {

			try {
				String pathToTesterFolder = String.format("tests/%s/", submission.getCourse());
				String pathToTesterRepo = submission.getGitTestRepo();
				File f = new File(pathToTesterFolder);

				if (!f.exists()) {
					logger.info("Checking for update for tester: {}", pathToTesterFolder);
					PriorityQueueService.halt(1); // only allow this job.. then continue to pull tests

					if (gitPullService.pullOrClone(pathToTesterFolder, pathToTesterRepo, Optional.empty())) {
						PriorityQueueService.go();
					} else {
						PriorityQueueService.go();
						reportService.reportFailedSubmission(submission, "No test files");
						return true;
					}
				}

				submissionPropertyService.rootProperties(submission); // preload initial configuration
			} catch (Exception e) {
				PriorityQueueService.go();
				String message = "Job execution failed for " + submission.getUniid() + " with message: " + e.getMessage();
				logger.error(message);
				reportService.reportFailedSubmission(submission, message);
				return true;
			}
		}

		if (submission.getGitStudentRepo() != null &&
				!submission.getSystemExtra().contains("skipCopyingStudent")
				&& !submission.getSystemExtra().contains("skipCopying")) {
			try {

				if (!gitPullService.repositoryMaintenance(submission)) {
					reportService.reportFailedSubmission(submission, submission.getResult());
					return true;
				}

			} catch (Exception e) {
				String message = "Job execution failed for " + submission.getUniid() + " with message: " + e.getMessage();
				logger.error(message);
				reportService.reportFailedSubmission(submission, message);
				return true;
			}
		}

		return false;
	}
}
