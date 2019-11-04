package ee.taltech.arete.service.submission;

import ee.taltech.arete.domain.Submission;

import java.util.List;


public interface SubmissionService {

	List<Submission> getSubmissions();
	Submission getSubmissionByHash(String hash);
	void saveSubmission(Submission submission);
}
