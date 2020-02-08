package ee.taltech.arete.service.submission;

import ee.taltech.arete.domain.Submission;


public interface SubmissionService {

    void populateAsyncFields(Submission submission);

    String populateSyncFields(Submission submission);

    String fixRepository(String url);

    void populateDefaultValues(Submission submission);

    void debugMode(boolean bool);

    boolean isDebug();
}
