package ee.taltech.arete.service.submission;

import ee.taltech.arete.domain.Submission;

import java.util.List;


public interface SubmissionService {

	void populateAsyncFields(Submission submission);

	String populateSyncFields(Submission submission);

	List<Submission> getSubmissions();

	List<Submission> getSubmissionByHash(String hash);

	void saveSubmission(Submission submission);

	void deleteSubmissionsAutomatically();
}
