package ee.taltech.arete.service.git;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.NoChangedFilesException;
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

		String home = System.getenv().getOrDefault("ARETE_HOME", System.getenv("HOME") + "/arete");
//		home = "C:\\Users\\envomp\\Documents\\TalTech\\automated-testing-service";

		String pathToStudentFolder = String.format("%s/students/%s/%s/", home, submission.getUniid(), submission.getProject());
		String pathToStudentRepo = String.format("https://gitlab.cs.ttu.ee/%s/%s.git", submission.getUniid(), submission.getProject());
		pullOrCloneStudentCode(submission, pathToStudentFolder, pathToStudentRepo);

		while (CONCURRENT) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (Exception ignored) {
			}
		}

		CONCURRENT = true;

		String pathToTesterFolder = String.format("%s/tests/%s/", home, submission.getProject());
		String pathToTesterRepo = String.format("https://gitlab.cs.ttu.ee/%s/%s.git", submission.getProject(), submission.getProjectBase());
		pullOrCloneTesterCode(submission, pathToTesterFolder, pathToTesterRepo);

		CONCURRENT = false;
	}

	@Override
	public void resetHead(Submission submission) {

		String home = System.getenv().getOrDefault("ARETE_HOME", System.getenv("HOME") + "/arete");

		String pathToStudentFolder = String.format("%s/students/%s/%s/", home, submission.getUniid(), submission.getProject());
		String pathToStudentRepo = String.format("https://gitlab.cs.ttu.ee/%s/%s.git", submission.getUniid(), submission.getProject());
		try {
			Git.open(new File(pathToStudentFolder)).reset().setMode(ResetCommand.ResetType.HARD).call();
		} catch (Exception e) {
			LOGGER.error("Failed to reset HEAD for student. Defaulting to hard reset: {}", e.getMessage());
			resetHard(submission, pathToStudentFolder, pathToStudentRepo);
		}
		pullOrCloneStudentCode(submission, pathToStudentFolder, pathToStudentRepo);

	}

	@Override
	public void pullOrCloneStudentCode(Submission submission, String pathToStudentFolder, String pathToStudentRepo) {
		Path path = Paths.get(pathToStudentFolder);

		if (Files.exists(path)) {
			LOGGER.info("Pulling a repository for student with uniid: {}", submission.getUniid());

			try {
				PullResult result = Git.open(new File(pathToStudentFolder)).pull()
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
								"envomp", System.getenv().get("GITLAB_PASSWORD")))
						.call();

				assert result.isSuccessful();

				String[] slugs = getChangedFolders(pathToStudentFolder);
				submission.setSlugs(slugs);

				LOGGER.info("Pulled a repository for student with uniid: {} and added slugs: {}", submission.getUniid(), submission.getSlugs());

			} catch (Exception e) {
				LOGGER.error("Pull failed for user: {} with message: {}. Cloning repository again.", submission.getUniid(), e.getMessage());

				resetHard(submission, pathToStudentFolder, pathToStudentRepo);
			}

		} else {
			cloneRepository(submission, pathToStudentFolder, pathToStudentRepo);
		}

		if (submission.getSlugs().length == 0) {
			throw new NoChangedFilesException("Check your folder names.");
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

			String[] slugs = getChangedFolders(pathToStudentFolder);
			submission.setSlugs(slugs);
			LOGGER.info("Cloned a repository for student with uniid: {} and added slugs: {}", submission.getUniid(), submission.getSlugs());

		} catch (Exception e) {
			LOGGER.error("Clone failed for user: {} with message: {}", submission.getUniid(), e.getMessage());
		}
	}

	@Override
	public void pullOrCloneTesterCode(Submission submission, String pathToTesterFolder, String pathToTesterRepo) {
		Path path = Paths.get(pathToTesterFolder);

		try {

			SafePullAndClone(submission, pathToTesterFolder, pathToTesterRepo, path);

		} catch (Exception e) {

			LOGGER.error("Failed getting tester. Defaulting to reset hard: {}", e.getMessage());
			resetHard(submission, pathToTesterFolder, pathToTesterRepo);

			try {
				SafePullAndClone(submission, pathToTesterFolder, pathToTesterRepo, path);
			} catch (Exception e2) {
				LOGGER.error("Completely failed to pull tester.");
			}
		}

	}

	private void SafePullAndClone(Submission submission, String pathToTesterFolder, String pathToTesterRepo, Path path) throws GitAPIException, IOException {
		if (Files.exists(path)) {
			LOGGER.info("Checking for tester update for project: {}", submission.getProject());

			try {
				PullResult result = Git.open(new File(pathToTesterFolder)).pull()
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
								"envomp", System.getenv().get("GITLAB_PASSWORD")))
						.call();

				assert result.isSuccessful();

			} catch (Exception e) {

				LOGGER.error("Checking failed for tester update. Trying to reset head and pull again: {}", e.getMessage());
				Git.open(new File(pathToTesterFolder)).reset().setMode(ResetCommand.ResetType.HARD).call();
				PullResult result = Git.open(new File(pathToTesterFolder)).pull()
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
								"envomp", System.getenv().get("GITLAB_PASSWORD")))
						.call();

				assert result.isSuccessful();
			}

		} else {
			LOGGER.info("Cloning tester with name: {}", submission.getProject());

			try {
				Git git = Git.cloneRepository()
						.setCredentialsProvider(
								new UsernamePasswordCredentialsProvider(
										"envomp", System.getenv().get("GITLAB_PASSWORD")))
						.setURI(pathToTesterRepo)
						.setDirectory(new File(pathToTesterFolder))
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
