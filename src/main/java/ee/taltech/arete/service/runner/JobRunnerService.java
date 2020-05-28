package ee.taltech.arete.service.runner;

import com.fasterxml.jackson.core.JsonProcessingException;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.domain.Submission;

import java.util.List;

public interface JobRunnerService {

	List<String> runJob(Submission submission);

	void reportSuccessfulSubmission(String slug, Submission submission, String output);

	AreteResponse getAreteResponse(String slug, Submission submission, String json) throws JsonProcessingException;
}
