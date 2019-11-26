package ee.taltech.arete.service.git;

import ee.taltech.arete.domain.Submission;

import java.io.IOException;

public interface GitPullService {

	 void repositoryMaintenance(Submission submission);

	void resetHead(Submission submission);

	void resetHard(Submission submission, String pathToStudentFolder, String pathToStudentRepo);

	void cloneRepository(Submission submission, String pathToStudentFolder, String pathToStudentRepo);

	void pullOrCloneStudentCode(Submission submission, String pathToStudentFolder, String pathToStudentRepo);

	void pullOrCloneTesterCode(Submission submission, String pathToTesterFolder, String pathToTesterRepo);

	String[] getChangedFolders(String pathToStudentFolder) throws IOException;

}
