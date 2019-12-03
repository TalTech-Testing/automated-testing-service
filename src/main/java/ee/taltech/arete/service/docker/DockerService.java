package ee.taltech.arete.service.docker;

import ee.taltech.arete.domain.Submission;

public interface DockerService {

	String runDocker(Submission submission, String slug) throws Exception;

}
