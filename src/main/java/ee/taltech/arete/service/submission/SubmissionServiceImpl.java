package ee.taltech.arete.service.submission;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.RequestFormatException;
import ee.taltech.arete.repository.SubmissionRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


@Service
public class SubmissionServiceImpl implements SubmissionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionService.class);

    @Autowired
    private SubmissionRepository submissionRepository;

    private Boolean DEBUG = true;

    private static String getRandomHash() {
        return RandomStringUtils.random(40, true, true).toLowerCase();
    }

    @Override
    public void populateAsyncFields(Submission submission) {

        populateTesterRelatedFields(submission);
        populateStudentRelatedFields(submission);
        populateDefaultValues(submission);

    }

    @Override
    public String populateSyncFields(Submission submission) {

        if (submission.getHash() == null) {
            submission.setHash(getRandomHash());
            submission.setWaitingroom(getRandomHash());
        } else {
            submission.setWaitingroom(submission.getHash()); // for integration tests
        }

        if (submission.getReturnUrl() == null || !submission.getReturnUrl().contains("waitingroom")) {
            submission.setReturnUrl(String.format("http://localhost:8098/waitingroom/%s", submission.getWaitingroom()));
        }

        populateTesterRelatedFields(submission);
        populateStudentRelatedFields(submission);
        populateDefaultValues(submission);

        return submission.getWaitingroom();
    }

    private void populateStudentRelatedFields(Submission submission) {
        try {
            if (submission.getGitStudentRepo() != null) {
                submission.setGitStudentRepo(fixRepository(submission.getGitStudentRepo()));
                String repo; //set OtherDefaults

                repo = submission.getGitStudentRepo().replaceAll("\\.git", "");
                String[] url = repo.replace("://", "").split("[/:]");

                if (submission.getUniid() == null) {
                    if (url[1].length() == 0) {
                        throw new BadRequestException("Git student repo namespace is size 0");
                    }
                    assert url[1].matches("^[0-9a-zA-Z]*$");
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
        } catch (Exception e) {
            throw new BadRequestException("Malformed student repo or student source.");
        }

    }

    private void populateTesterRelatedFields(Submission submission) {

        try {
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
        } catch (Exception e) {
            throw new BadRequestException("Malformed git test repo or test source.");
        }


    }

    @Override
    public String fixRepository(String url) {
        if (System.getenv().containsKey("GITLAB_PASSWORD")) {
            if (url.startsWith("git")) {
                url = url.replaceFirst(":", "/");
                url = url.replace("git@", "https://");
            }

        } else {
            if (url.startsWith("http")) {
                url = url.replace("https://", "git@");
                url = url.replaceFirst("/", ":");
            }
        }
        if (!url.endsWith(".git")) {
            return url + ".git";
        }
        return url;
    }

    private void populateDefaultValues(Submission submission) {
        if (submission.getHash() != null && !submission.getHash().matches("^[a-zA-Z0-9]+$")) {
            submission.setHash(getRandomHash()); // in case of a faulty input
        }

        if (submission.getPriority() == null) {
            submission.setPriority(5);
        }

        if (submission.getTimestamp() == null) {
            submission.setTimestamp(System.currentTimeMillis());
        }

        if (submission.getDockerTimeout() == null) {
            if (DEBUG) {
                submission.setDockerTimeout(360); // 360 sec
            } else {
                submission.setDockerTimeout(120); // 120 sec
            }
        }

        if (submission.getDockerExtra() == null) {
            submission.setDockerExtra(new HashSet<>());
        }

        if (submission.getSystemExtra() == null) {
            submission.setSystemExtra(new HashSet<>());
        }

        if (submission.getUniid() == null) {
            submission.getSystemExtra().add("noMail");
        }
    }

    @Override
    public List<Submission> getSubmissions() {
        LOGGER.info("Reading all Submissions from database.");
        return submissionRepository.findAll();
    }

    @Override
    public List<Submission> getSubmissionByHash(String hash) {
        ArrayList<Submission> submissions = submissionRepository.findByHash(hash);
        LOGGER.info("Reading Submission hash " + hash + " from database.");
        if (submissions.size() > 0) {
            return submissions;
        }
        LOGGER.error(String.format("Submission with hash %s was not found.", hash));
        throw new RequestFormatException(String.format("No Submission with hash: %s was not found", hash));
    }

    @Override
    public void saveSubmission(Submission submission) {
        submissionRepository.saveAndFlush(submission);
        LOGGER.info("Submission with hash {} successfully saved into DB", submission.getHash());
    }

    @Override
    @Scheduled(cron = "0 4 4 * * ?")
    public void deleteSubmissionsAutomatically() {
//		for (Submission submission : submissionRepository.findAll()) {
//			if (System.currentTimeMillis() - submission.getTimestamp() > (1000 * 60 * 60 * 24 * 7)) { // if it has been a week
//				submissionRepository.delete(submission);
//				LOG.info("Deleted old submission from DB: {}", submission);
//			}
//		}
    }

    @Override
    public void debugMode(boolean bool) {
        this.DEBUG = bool;
    }

    @Override
    public boolean isDebug() {
        return DEBUG;
    }

}
