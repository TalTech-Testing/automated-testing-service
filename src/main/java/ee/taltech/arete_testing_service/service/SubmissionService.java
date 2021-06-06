package ee.taltech.arete_testing_service.service;

import ee.taltech.arete_testing_service.configuration.ServerConfiguration;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.exception.RequestFormatException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;


@Service
@AllArgsConstructor
public class SubmissionService {

    private final Logger logger;
    private final ServerConfiguration serverConfiguration;

    private static String getRandomHash() {
        return RandomStringUtils.random(40, true, true).toLowerCase(); // git hash is 40 long
    }

    public void populateAsyncFields(Submission submission) {
        populateTesterRelatedFields(submission);
        populateStudentRelatedFields(submission);
        populateDefaultValues(submission);
    }

    public void populateTesterRelatedFields(Submission submission) {
        if (submission.getGitTestRepo() != null) {
            try {
                submission.setGitTestRepo(fixRepository(submission.getGitTestRepo()));
                String namespace = submission.getGitTestRepo()
                        .replace(".git", "")
                        .replace("://", "")
                        .split("[:/]", 2)[1];

                if (namespace.length() == 0) {
                    logger.warn("Git test source namespace is needed size non zero. {}", submission.getGitTestRepo());
                    throw new RequestFormatException("Git test source namespace is needed size non zero.");
                }

                if (submission.getCourse() == null) {
                    submission.setCourse(namespace);
                }

                return;

            } catch (Exception e) {
                throw new RequestFormatException(e.getMessage());
            }
        }

        if (submission.getSystemExtra() != null &&
                (submission.getSystemExtra().contains("skipCopyingTests")
                        || submission.getSystemExtra().contains("skipCopying"))) {
            submission.setGitTestRepo(submission.getTestingPlatform());
            return;
        }

        if (submission.getTestSource() != null) {

            if (submission.getTestSource().size() == 0) {
                logger.warn("Test source is needed size non zero. {}", submission.getGitTestRepo());
                throw new RequestFormatException("Test source is needed size non zero.");
            }

        } else {
            logger.warn("Git test repo or test source is needed. {}", submission.getGitTestRepo());
            throw new RequestFormatException("Git test repo or test source is needed.");
        }

    }

    public void populateStudentRelatedFields(Submission submission) {

        if (submission.getGitStudentRepo() != null) {
            submission.setGitStudentRepo(fixRepository(submission.getGitStudentRepo()));
            String repo; //set OtherDefaults

            repo = submission.getGitStudentRepo().replaceAll("\\.git", "");
            String[] url = repo.replace("://", "").split("[/:]");

            if (submission.getUniid() == null) {
                if (url[1].length() == 0) {
                    logger.warn("Git student repo namespace is size 0. {}", submission.getGitStudentRepo());
                    throw new RequestFormatException("Git student repo namespace is size 0");
                }
                assert url[1].matches(serverConfiguration.getNameMatcher());
                submission.setUniid(url[1]); // user identificator - this is 100% unique
            }

            if (submission.getFolder() == null) {
                if (url[url.length - 1].length() == 0) {
                    logger.warn("Git student repo namespace with path is size 0. {}", submission.getGitStudentRepo());
                    throw new RequestFormatException("Git student repo namespace with path is size 0");
                }
                submission.setFolder(url[url.length - 1]); // Just the folder where file is saved - user cant have multiple of those
            }

            return;
        }

        if (submission.getSystemExtra() != null &&
                (submission.getSystemExtra().contains("skipCopyingStudent")
                        || submission.getSystemExtra().contains("skipCopying"))) {
            submission.setGitTestRepo(submission.getTestingPlatform());
            return;
        }

        if (submission.getSource() != null) {

            if (submission.getSource().size() == 0) {
                throw new RequestFormatException("Source is needed size non zero.");
            }

            if (submission.getSlugs() == null) {
                String path = submission.getSource().get(0).getPath().split("\\\\")[0];
                if (path.equals(submission.getSource().get(0).getPath())) {
                    path = submission.getSource().get(0).getPath().split("/")[0];
                }
                submission.setSlugs(new HashSet<>(Collections.singletonList(path)));
            }

        } else {
            logger.warn("Git student repo or student source is needed. {}", submission.getGitStudentRepo());
            throw new RequestFormatException("Git student repo or student source is needed.");
        }
    }

    public void populateDefaultValues(Submission submission) {
        if (submission.getHash() == null || !submission.getHash().matches("^[a-zA-Z0-9]+$")) {
            submission.setHash(getRandomHash()); // in case of a faulty input
        }

        if (submission.getPriority() == null) {
            submission.setPriority(5);
        }

        if (submission.getTimestamp() == null) {
            submission.setTimestamp(System.currentTimeMillis());
        }
        submission.setReceivedTimestamp(System.currentTimeMillis());

        if (submission.getDockerTimeout() == null) {
            submission.setDockerTimeout(serverConfiguration.getDefaultDockerTimeout()); // 120 sec
        }

        if (submission.getSystemExtra() == null) {
            submission.setSystemExtra(new HashSet<>());
        }

        if (submission.getUniid() == null) {
            throw new RequestFormatException("uniid is required");
        }

        if (submission.getEmail() == null) {
            submission.setEmail(submission.getUniid() + "@ttu.ee");
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

    public String populateSyncFields(Submission submission) {
        String hash = getRandomHash();
        submission.setWaitingroom(hash);
        submission.setReturnUrl(String.format("http://127.0.0.1:8098/waitingroom/%s", hash));

        populateTesterRelatedFields(submission);
        populateStudentRelatedFields(submission);
        populateDefaultValues(submission);

        return submission.getWaitingroom();
    }

}
