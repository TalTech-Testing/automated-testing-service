package ee.taltech.arete.service.submission;

import ee.taltech.arete.domain.Submission;

import java.util.List;


public interface SubmissionService {

	void populateFields(Submission submission);

	List<Submission> getSubmissions();

	Submission getSubmissionByHash(String hash);

	void saveSubmission(Submission submission);
}
