package ee.taltech.arete.service.git;

import ee.taltech.arete.domain.Submission;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

public interface GitPullService {

	void repositoryMaintenance(Submission submission);

	boolean resetHead(Submission submission);

	boolean pullOrClone(String pathToFolder, String pathToRepo, Optional<Submission> submission) throws GitAPIException, IOException;

	HashSet<String> getChangedFolders(String pathToFolder) throws IOException;

}
