package ee.taltech.arete.service.runner;

import ee.taltech.arete.domain.Submission;

import java.util.List;

public interface JobRunnerService {

    List<String> runJob(Submission submission);

    void clearInputAndOutput(Submission submission, String output);
}
