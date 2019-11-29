package ee.taltech.arete.service.git;

import ee.taltech.arete.domain.Submission;

import java.io.IOException;

public interface GitPullService {

	void repositoryMaintenance(Submission submission);

	void resetHead(Submission submission);

	/// Try not to use the following methods.

	void resetHard(Submission submission, String pathToFolder, String pathToRepo);

	void cloneRepository(Submission submission, String pathToFolder, String pathToRepo);

	void pullOrCloneStudentCode(Submission submission, String pathToFolder, String pathToRepo);

	void pullOrCloneTesterCode(Submission submission, String pathToFolder, String pathToRepo);

	String[] getChangedFolders(String pathToFolder) throws IOException;

}
