package ee.taltech.arete.service;

import ee.taltech.arete.configuration.DevProperties;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.RequestFormatException;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.BadRequestException;
import java.util.Collections;
import java.util.HashSet;


@Service
public class SubmissionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionService.class);

	private final DevProperties devProperties;
	private final JobRunnerService jobRunnerService;

	public SubmissionService(DevProperties devProperties, JobRunnerService jobRunnerService) {
		this.devProperties = devProperties;
		this.jobRunnerService = jobRunnerService;
	}

	private static String getRandomHash() {
		return RandomStringUtils.random(40, true, true).toLowerCase(); // git hash is 40 long
	}

	public void populateAsyncFields(Submission submission) {
		populateTesterRelatedFields(submission);
		populateStudentRelatedFields(submission);
		populateDefaultValues(submission);
		jobRunnerService.rootProperties(submission);
	}

	public String populateSyncFields(Submission submission) {

		// for integration tests
		if (submission.getHash() == null) {
			submission.setHash(getRandomHash());
		}
		submission.setWaitingroom(submission.getHash());

		if (submission.getReturnUrl() == null || !submission.getReturnUrl().contains("waitingroom")) {
			submission.setReturnUrl(String.format("http://localhost:8098/waitingroom/%s", submission.getWaitingroom()));
		}

		populateTesterRelatedFields(submission);
		populateStudentRelatedFields(submission);
		populateDefaultValues(submission);
		jobRunnerService.rootProperties(submission);

		return submission.getWaitingroom();
	}

	private void populateStudentRelatedFields(Submission submission) {
		if (submission.getGitStudentRepo() != null) {
			submission.setGitStudentRepo(fixRepository(submission.getGitStudentRepo()));
			String repo; //set OtherDefaults

			repo = submission.getGitStudentRepo().replaceAll("\\.git", "");
			String[] url = repo.replace("://", "").split("[/:]");

			if (submission.getUniid() == null) {
				if (url[1].length() == 0) {
					throw new BadRequestException("Git student repo namespace is size 0");
				}
				assert url[1].matches(devProperties.getNameMatcher());
				submission.setUniid(url[1]); // user identificator - this is 100% unique
			}

			if (submission.getFolder() == null) {
				if (url[url.length - 1].length() == 0) {
					throw new BadRequestException("Git student repo namespace with path is size 0");
				}
				submission.setFolder(url[url.length - 1]); // Just the folder where file is saved - user cant have multiple of those
			}

		} else if (submission.getSource() != null) {

			if (submission.getSlugs() == null) {
				String path = submission.getSource().get(0).getPath().split("\\\\")[0];
				if (path.equals(submission.getSource().get(0).getPath())) {
					path = submission.getSource().get(0).getPath().split("/")[0];
				}
				submission.setSlugs(new HashSet<>(Collections.singletonList(path)));
			}

		} else {
			throw new BadRequestException("Git student repo or student source is needed.");
		}
	}

	private void populateTesterRelatedFields(Submission submission) {
		if (submission.getGitTestSource() != null) {
			try {
				submission.setGitTestSource(fixRepository(submission.getGitTestSource()));
				String namespace = submission.getGitTestSource()
						.replace(".git", "")
						.replace("://", "")
						.split("[:/]", 2)[1];

				if (namespace.length() == 0) {
					throw new BadRequestException("Git test source namespace is needed size non zero.");
				}

				if (submission.getCourse() == null) {
					submission.setCourse(namespace);
				}

			} catch (Exception e) {
				throw new BadRequestException(e.getMessage());
			}
		} else if (submission.getTestSource() != null) {

			if (submission.getTestSource().size() == 0) {
				throw new BadRequestException("Test source is needed size non zero.");
			}

		} else {
			throw new BadRequestException("Git test repo or test source is needed.");
		}
	}

	public String fixRepository(String url) {
		if (System.getenv().containsKey("GIT_PASSWORD")) {
			if (url.startsWith("git")) {
				url = url.replaceFirst(":", "/");
				url = url.replace("git@", "https://");
			}
		} else {
			if (url.startsWith("http")) {
				url = url.replace("https://", "git@");
				url = url.replace("http://", "git@");
				url = url.replaceFirst("/", ":");
			}
			if (!url.contains(":")) {
				url = url.replaceFirst("/", ":");
			}
		}

		if (!url.endsWith(".git")) {
			return url + ".git";
		}
		return url;
	}


	public void populateDefaultValues(Submission submission) {
		if (submission.getHash() != null && !submission.getHash().matches("^[a-zA-Z0-9]+$")) {
			submission.setHash(getRandomHash()); // in case of a faulty input
		}

		if (submission.getPriority() == null) {
			submission.setPriority(5);
		}

		if (submission.getTimestamp() == null) {
			submission.setTimestamp(System.currentTimeMillis());
		}
		submission.setRecievedTimeStamp(System.currentTimeMillis());

		if (submission.getDockerTimeout() == null) {
			if (devProperties.getDebug()) {
				submission.setDockerTimeout(360); // 360 sec
			} else {
				submission.setDockerTimeout(devProperties.getDefaultDockerTimeout()); // 120 sec
			}
		}

		if (submission.getDockerExtra() == null) {
			submission.setDockerExtra(new HashSet<>());
		}

		if (submission.getSystemExtra() == null) {
			submission.setSystemExtra(new HashSet<>());
		}

		if (submission.getUniid() == null) {
			throw new RequestFormatException("uniid is required");
		}

		if (submission.getEmail() == null) {
			submission.setEmail(submission.getUniid() + "@ttu.ee"); // default
		}

	}

}
