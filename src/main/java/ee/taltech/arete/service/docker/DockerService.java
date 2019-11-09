package ee.taltech.arete.service.docker;

import ee.taltech.arete.domain.Submission;

public interface DockerService {

	void runDocker(Submission submission, String slug);

}
