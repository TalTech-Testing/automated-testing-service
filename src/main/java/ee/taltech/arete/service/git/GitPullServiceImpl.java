package ee.taltech.arete.service.git;

import ee.taltech.arete.domain.Submission;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GitPullServiceImpl implements GitPullService {

	private static final List<String> TESTABLES = List.of("ADD", "MODIFY");
	private static Logger LOGGER = LoggerFactory.getLogger(GitPullService.class);
	private boolean CONCURRENT = false;

	@Override
	public void repositoryMaintenance(Submission submission) {

		String pathToStudentFolder = String.format("students/%s/%s/", submission.getUniid(), submission.getProject());
		String pathToStudentRepo = String.format("https://gitlab.cs.ttu.ee/%s/%s.git", submission.getUniid(), submission.getProject());

		try {
			LOGGER.info("Checking for update for student:");
			pullOrClone(submission, pathToStudentFolder, pathToStudentRepo);
			submission.setSlugs(getChangedFolders(pathToStudentFolder));
		} catch (IOException e) {
			LOGGER.error("Failed to read student repository.");
		}

		while (CONCURRENT) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (Exception ignored) {
			}
		}

		CONCURRENT = true;
		try {
			String pathToTesterFolder = String.format("tests/%s/", submission.getProject());
			String pathToTesterRepo = String.format("https://gitlab.cs.ttu.ee/%s/%s.git", submission.getProject(), submission.getProjectBase());
			LOGGER.info("Checking for update for tester:");
			pullOrClone(submission, pathToTesterFolder, pathToTesterRepo);
		} finally {
			CONCURRENT = false;
		}
	}

	@Override
	public void resetHead(Submission submission) {

		String pathToStudentFolder = String.format("students/%s/%s/", submission.getUniid(), submission.getProject());
		String pathToStudentRepo = String.format("https://gitlab.cs.ttu.ee/%s/%s.git", submission.getUniid(), submission.getProject());
		try {
			Git.open(new File(pathToStudentFolder)).reset().setMode(ResetCommand.ResetType.HARD).call();
		} catch (Exception e) {
			LOGGER.error("Failed to reset HEAD for student. Defaulting to hard reset: {}", e.getMessage());
			resetHard(submission, pathToStudentFolder, pathToStudentRepo);
		}
	}


	@Override
	public void resetHard(Submission submission, String pathToFolder, String pathToRepo) {

		try {
			FileUtils.deleteDirectory(new File(pathToFolder));
		} catch (Exception e1) {
			throw new ConcurrentModificationException("Folder is already in use and is corrupted at the same time. Try pushing less often there, buddy. :)"); //Never actually gets here.
		}

		cloneRepository(submission, pathToFolder, pathToRepo);
	}

	@Override
	public void cloneRepository(Submission submission, String pathToStudentFolder, String pathToStudentRepo) {
		LOGGER.info("Cloning a repository for student with uniid: {}", submission.getUniid());

		try {
			Git git = Git.cloneRepository()
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(
									"envomp", System.getenv().get("GITLAB_PASSWORD")))
					.setURI(pathToStudentRepo)
					.setDirectory(new File(pathToStudentFolder))
					.call();

			LOGGER.info("Cloned a repository for student with uniid: {} and added slugs: {}", submission.getUniid(), submission.getSlugs());

		} catch (Exception e) {
			LOGGER.error("Clone failed for user: {} with message: {}", submission.getUniid(), e.getMessage());
		}
	}

	@Override
	public void pullOrClone(Submission submission, String pathToTesterFolder, String pathToTesterRepo) {

		try {

			SafePullAndClone(submission, pathToTesterFolder, pathToTesterRepo);

		} catch (Exception e) {

			LOGGER.error("Failed getting tester. Defaulting to reset hard: {}", e.getMessage());
			resetHard(submission, pathToTesterFolder, pathToTesterRepo);

			try {
				SafePullAndClone(submission, pathToTesterFolder, pathToTesterRepo);
			} catch (Exception e2) {
				LOGGER.error("Completely failed to pull tester.");
			}
		}

	}

	private void SafePullAndClone(Submission submission, String pathToFolder, String pathToRepo) throws GitAPIException, IOException {

		Path path = Paths.get(pathToFolder);

		if (Files.exists(path)) {
			LOGGER.info("Checking for update for project: {}", pathToFolder);

			try {
				PullResult result = Git.open(new File(pathToFolder)).pull()
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
								"envomp", System.getenv().get("GITLAB_PASSWORD")))
						.call();

				assert result.isSuccessful();

			} catch (Exception e) {

				LOGGER.error("Checking failed for tester update. Trying to reset head and pull again: {}", e.getMessage());
				Git.open(new File(pathToFolder)).reset().setMode(ResetCommand.ResetType.HARD).call();
				PullResult result = Git.open(new File(pathToFolder)).pull()
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
								"envomp", System.getenv().get("GITLAB_PASSWORD")))
						.call();

				assert result.isSuccessful();
			}

		} else {
			LOGGER.info("Cloning with name: {}", submission.getProject());

			try {
				Git git = Git.cloneRepository()
						.setCredentialsProvider(
								new UsernamePasswordCredentialsProvider(
										"envomp", System.getenv().get("GITLAB_PASSWORD")))
						.setURI(pathToRepo)
						.setDirectory(new File(pathToFolder))
						.call();

			} catch (Exception e) {
				LOGGER.error("Cloning tester failed with message: {}", e.getMessage());
				throw new ExceptionInInitializerError();
			}
		}
	}

	@Override
	public String[] getChangedFolders(String pathToStudentFolder) throws IOException {
		HashSet<String> repoMainFolders = new HashSet<>();
		Repository repository = new FileRepository(pathToStudentFolder + ".git");
		RevWalk rw = new RevWalk(repository);
		ObjectId head = repository.resolve(Constants.HEAD);
		RevCommit commit = rw.parseCommit(head);
		RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);
		List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
		for (DiffEntry diff : diffs) {
			if (TESTABLES.contains(diff.getChangeType().name())) {
				String potentialSlug = diff.getNewPath().split("/")[0];
				if (potentialSlug.matches("[a-zA-Z0-9_]*")) {
					repoMainFolders.add(potentialSlug);
				}
			}
		}
		return repoMainFolders.toArray(new String[0]);
	}
}
