package ee.taltech.arete.service.git;

import ee.taltech.arete.domain.Submission;

public interface GitPullService {

	void repositoryMaintenance(Submission submission);

	void pullOrCloneStudentCode(Submission submission, String pathToStudentFolder, String pathToStudentRepo);

	void pullOrCloneTesterCode(Submission submission, String pathToTesterFolder, String pathToTesterRepo);

}
