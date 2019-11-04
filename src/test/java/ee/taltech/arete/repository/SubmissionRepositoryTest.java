package ee.taltech.arete.repository;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.SubmissionService;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringRunner.class)
@DataJpaTest
class SubmissionRepositoryTest {

    private Submission submission;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private SubmissionService submissionService;

    @Before
    public void init() {
        submission = new Submission("envomp", "hash", "python", "neti.ee", new String[]{"style"});
        entityManager.persist(submission);
    }

    @Test
    public void testSubmission() throws Exception {
        assert submissionService.getSubmissionByHash("hash").equals(submission);
    }

}