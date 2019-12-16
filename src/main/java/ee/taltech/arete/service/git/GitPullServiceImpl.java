package ee.taltech.arete.service.git;

import ee.taltech.arete.domain.Submission;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
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
import java.util.Optional;

@Service
public class GitPullServiceImpl implements GitPullService {

	private static final List<String> TESTABLES = List.of("ADD", "MODIFY");
	private static Logger LOGGER = LoggerFactory.getLogger(GitPullService.class);
	private static TransportConfigCallback transportConfigCallback = new SshTransportConfigCallback();


	@Override
	public void repositoryMaintenance(Submission submission) {

		String pathToStudentFolder = String.format("students/%s/%s/", submission.getUniid(), submission.getProject());

		try {
			pullOrClone(pathToStudentFolder, submission.getGitStudentRepo(), Optional.of(submission));
			submission.setSlugs(getChangedFolders(pathToStudentFolder));
		} catch (IOException e) {
			LOGGER.error("Failed to read student repository.");
		}

	}

	private void resetHard(String pathToFolder, String pathToRepo) {

		try {
			FileUtils.deleteDirectory(new File(pathToFolder));
		} catch (Exception e1) {
			throw new ConcurrentModificationException("Folder is already in use and is corrupted at the same time. Try pushing less often there, buddy. :)"); //Never actually gets here. Unless it does.
		}

	}

	@Override
	public void pullOrClone(String pathToFolder, String pathToRepo, Optional<Submission> submission) {

		try {

			SafePullAndClone(pathToFolder, pathToRepo, submission);

		} catch (Exception e) {

			LOGGER.error("Defaulting to reset hard: {}", e.getMessage());
			resetHard(pathToFolder, pathToRepo);

			try {
				SafePullAndClone(pathToFolder, pathToRepo, submission);
			} catch (Exception e2) {
				LOGGER.error("Completely failed to pull or clone.");
			}
		}

	}

	private void SafePullAndClone(String pathToFolder, String pathToRepo, Optional<Submission> submission) throws GitAPIException, IOException {
		Path path = Paths.get(pathToFolder);

		if (Files.exists(path)) {
			LOGGER.info("Checking for update for project: {}", pathToFolder);

			try {

				SafePull(pathToFolder, submission);

			} catch (Exception e) {
				LOGGER.error("Checking failed for update. Trying to reset head and pull again: {}", e.getMessage());
				Git.open(new File(pathToFolder)).reset().setMode(ResetCommand.ResetType.HARD).call();
				SafePull(pathToFolder, submission);
			}

		} else {

			SafeClone(pathToFolder, pathToRepo);
			LOGGER.info("Cloned to folder: {}", pathToFolder);

			if (submission.isPresent()) {
				SafePull(pathToFolder, submission);
			}


		}
	}

	private void SafeClone(String pathToFolder, String pathToRepo) throws GitAPIException {
		try {
			if (System.getenv().containsKey("GITLAB_PASSWORD")) {
				Git git = Git.cloneRepository()
						.setCredentialsProvider(
								new UsernamePasswordCredentialsProvider(
										"envomp", System.getenv().get("GITLAB_PASSWORD"))) // integration testing only pls.
						.setURI(pathToRepo)
						.setDirectory(new File(pathToFolder))
						.call();

			} else {
				Git git = Git.cloneRepository()
						.setTransportConfigCallback(transportConfigCallback)
						.setURI(pathToRepo)
						.setDirectory(new File(pathToFolder))
						.call();
			}
		} catch (Exception e) {
			LOGGER.error("Cloning failed with message: {}", e.getMessage());
			throw new ExceptionInInitializerError(e.getMessage());
		}
	}

	@Override
	public void resetHead(Submission submission) {

		String pathToStudentFolder = String.format("students/%s/%s/", submission.getUniid(), submission.getProject());
		try {
			Git.open(new File(pathToStudentFolder)).reset().setMode(ResetCommand.ResetType.HARD).call();
		} catch (Exception e) {
			LOGGER.error("Failed to reset HEAD for student. Defaulting to hard reset: {}", e.getMessage());
			resetHard(pathToStudentFolder, submission.getGitStudentRepo());
		}
	}

	private void SafePull(String pathToFolder, Optional<Submission> submission) throws GitAPIException, IOException {

		try {
			Git git = Git.open(new File(pathToFolder));
			if (submission.isPresent()) {
				Submission user = submission.get();

				if (submission.get().getHash() != null) {

					try {
						fetch(git);
						reset(git, user);

						LOGGER.info("Pulled specific hash {} for user {}", user.getHash(), user.getUniid());
					} catch (Exception e) {
						fixHash(git, user);
						fetch(git);
						reset(git, user);
					}

				} else {

					SafePull(git);
					user.setHash(getLatestHash(git));
					LOGGER.info("Pulled for user {} and set hash {}", user.getUniid(), user.getHash());
				}

			} else {

				SafePull(git);
				LOGGER.info("Pulled to {} with hash {}", pathToFolder, getLatestHash(git));
			}
		} catch (Exception e) {
			LOGGER.error("Pull failed with message: {}", e.getMessage());
			throw new ExceptionInInitializerError(e.getMessage());
		}
	}

	private void reset(Git git, Submission user) throws GitAPIException {
		Ref command = git.reset().setMode(ResetCommand.ResetType.HARD).setRef(user.getHash()).call();
	}

	private void SafePull(Git git) throws GitAPIException {
		PullResult result;
		if (System.getenv().containsKey("GITLAB_PASSWORD")) {
			result = git.pull()
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
							"envomp", System.getenv().get("GITLAB_PASSWORD")))
					.call();

		} else {
			result = git.pull()
					.setTransportConfigCallback(transportConfigCallback)
					.call();
		}

		assert result.isSuccessful();
	}

	private void fetch(Git git) throws GitAPIException {
		if (System.getenv().containsKey("GITLAB_PASSWORD")) {
			FetchResult result = git.fetch().setRemote("origin")
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
							"envomp", System.getenv().get("GITLAB_PASSWORD")))
					.call();

		} else {
			FetchResult result = git.fetch().setRemote("origin")
					.setTransportConfigCallback(transportConfigCallback)
					.call();
		}
	}

	private String getLatestHash(Git git) throws GitAPIException, IOException {
		RevCommit youngestCommit = null;
		List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
		try (RevWalk walk = new RevWalk(git.getRepository())) {
			for (Ref branch : branches) {
				RevCommit commit = walk.parseCommit(branch.getObjectId());
				if (youngestCommit == null || commit.getAuthorIdent().getWhen().compareTo(
						youngestCommit.getAuthorIdent().getWhen()) > 0)
					youngestCommit = commit;
			}
		}

		assert youngestCommit != null;
		return youngestCommit.name();
	}

	private void fixHash(Git git, Submission user) throws GitAPIException, IOException {
		RevCommit youngestCommit = null;
		List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
		try (RevWalk walk = new RevWalk(git.getRepository())) {
			for (Ref branch : branches) {
				RevCommit commit = walk.parseCommit(branch.getObjectId());
				if (youngestCommit == null || commit.getAuthorIdent().getWhen().compareTo(youngestCommit.getAuthorIdent().getWhen()) > 0) {
					if (commit.name().equals(user.getHash())) {
						return;
					}
					youngestCommit = commit;
				}
			}
		}
		assert youngestCommit != null;
		LOGGER.error("Detected faulty hash {}, replaced it with a correct one {}", user.getHash(), youngestCommit.name());
		user.setHash(youngestCommit.name());
	}

	@Override
	public HashSet<String> getChangedFolders(String pathToStudentFolder) throws IOException {
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
		return repoMainFolders;
	}
}