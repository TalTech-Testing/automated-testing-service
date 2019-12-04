package ee.taltech.arete.service.submission;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.RequestFormatException;
import ee.taltech.arete.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class SubmissionServiceImpl implements SubmissionService {

	private static final Logger LOG = LoggerFactory.getLogger(SubmissionService.class);
	@Autowired
	private ObjectMapper jacksonObjectMapper;
	@Autowired
	private SubmissionRepository submissionRepository;

	@Override
	public void populateFields(Submission submission) {
		if (submission.getPriority() == null) {
			submission.setPriority(5);
		}

		if (submission.getTimestamp() == null) {
			submission.setTimestamp(System.currentTimeMillis());
		}

		if (submission.getDockerExtra() == null) {
			submission.setDockerExtra(new String[]{"stylecheck"});
		}

		if (submission.getProjectBase() == null) {
			submission.setProjectBase("ex");
		}

		if (submission.getDockerTimeout() == null) {
			submission.setDockerTimeout(120); // 120 sec
		}

		if (submission.getSystemExtra() == null) {
			submission.setSystemExtra(new String[]{});
		}

	}

	@Override
	public List<Submission> getSubmissions() {
		LOG.info("Reading all Submissions from database.");
		return submissionRepository.findAll();
	}

	@Override
	public List<Submission> getSubmissionByHash(String hash) {
		ArrayList<Submission> submissions = submissionRepository.findByHash(hash);
		LOG.info("Reading Submission hash " + hash + " from database.");
		if (submissions.size() > 0) {
			return submissions;
		}
		LOG.error(String.format("Submission with hash %s was not found.", hash));
		throw new RequestFormatException(String.format("No Submission with hash: %s was not found", hash));
	}

	@Override
	public void saveSubmission(Submission submission) {
		submissionRepository.saveAndFlush(submission);
		LOG.info("Submission with hash {} successfully saved into DB", submission.getHash());
	}

	@Override
	@Scheduled(cron = "0 4 4 * * ?")
	public void deleteSubmissionsAutomatically() {
//		for (Submission submission : submissionRepository.findAll()) {
//			if (System.currentTimeMillis() - submission.getTimestamp() > (1000 * 60 * 60 * 24 * 7)) { // if it has been a week
//				submissionRepository.delete(submission);
//				LOG.info("Deleted old submission from DB: {}", submission);
//			}
//		}
	}
}
