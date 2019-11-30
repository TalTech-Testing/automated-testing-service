package ee.taltech.arete.service.git;

import ee.taltech.arete.domain.Submission;

import java.io.IOException;
import java.util.Optional;

public interface GitPullService {

	void repositoryMaintenance(Submission submission);

	void resetHead(Submission submission);

	/// Try not to use the following methods. Or when you need to use em. Use with caution. Modifying files concurrently leads to a hot mess.

	void resetHard(String pathToFolder, String pathToRepo);

	void cloneRepository(String pathToFolder, String pathToRepo);

	void pullOrClone(String pathToFolder, String pathToRepo, Optional<Submission> submission);

	String[] getChangedFolders(String pathToFolder) throws IOException;

}
