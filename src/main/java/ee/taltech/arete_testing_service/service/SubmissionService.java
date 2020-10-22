package ee.taltech.arete_testing_service.service;

import ee.taltech.arete_testing_service.configuration.DevProperties;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.exception.RequestFormatException;
import ee.taltech.arete.java.TestingEnvironment;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

        if (!submission.getSystemExtra().contains("integration_tests")) {
            submission.setHash(getRandomHash());
            submission.setWaitingroom(submission.getHash());
            submission.setReturnUrl(String.format("http://localhost:8098/waitingroom/%s", submission.getWaitingroom()));
        } else {
            String[] parts = submission.getReturnUrl().split("/");
            submission.setWaitingroom(parts[parts.length - 1]);
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
                    throw new RequestFormatException("Git student repo namespace is size 0");
                }
                assert url[1].matches(devProperties.getNameMatcher());
                submission.setUniid(url[1]); // user identificator - this is 100% unique
            }

            if (submission.getFolder() == null) {
                if (url[url.length - 1].length() == 0) {
                    throw new RequestFormatException("Git student repo namespace with path is size 0");
                }
                submission.setFolder(url[url.length - 1]); // Just the folder where file is saved - user cant have multiple of those
            }

        } else if (submission.getSource() != null) {

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
            throw new RequestFormatException("Git student repo or student source is needed.");
        }
    }

    private void populateTesterRelatedFields(Submission submission) {
        if (submission.getTestingEnvironment() == TestingEnvironment.DOCKER) {
            if (submission.getGitTestRepo() != null) {
                try {
                    submission.setGitTestRepo(fixRepository(submission.getGitTestRepo()));
                    String namespace = submission.getGitTestRepo()
                            .replace(".git", "")
                            .replace("://", "")
                            .split("[:/]", 2)[1];

                    if (namespace.length() == 0) {
                        throw new RequestFormatException("Git test source namespace is needed size non zero.");
                    }

                    if (submission.getCourse() == null) {
                        submission.setCourse(namespace);
                    }

                } catch (Exception e) {
                    throw new RequestFormatException(e.getMessage());
                }
            } else if (submission.getTestSource() != null) {

                if (submission.getTestSource().size() == 0) {
                    throw new RequestFormatException("Test source is needed size non zero.");
                }

            } else {
                throw new RequestFormatException("Git test repo or test source is needed.");
            }
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
        submission.setReceivedTimestamp(System.currentTimeMillis());

        if (submission.getDockerTimeout() == null) {
            submission.setDockerTimeout(devProperties.getDefaultDockerTimeout()); // 120 sec
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
            submission.setEmail(submission.getUniid() + "@ttu.ee");
        }

        if (submission.getTestingEnvironment() == null) {
            submission.setTestingEnvironment(TestingEnvironment.DOCKER);
        }

    }

}
