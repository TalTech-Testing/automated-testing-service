package ee.taltech.arete.service.runner;

import ee.taltech.arete.domain.Submission;

public interface JobRunnerService {

    void runJob(Submission submission);
    void pullJobRequirements(Submission submission);
    void runDocker(Submission submission, String slug);

}
