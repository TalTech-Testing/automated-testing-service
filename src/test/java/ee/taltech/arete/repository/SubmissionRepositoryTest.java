package ee.taltech.arete.repository;

import ee.taltech.arete.domain.Submission;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static ee.taltech.arete.initializers.SubmissionInitializer.getControllerEndpointSubmission;


@RunWith(SpringRunner.class)
@DataJpaTest
public class SubmissionRepositoryTest {

	private static Logger LOGGER = LoggerFactory.getLogger(Test.class);

	private Submission submission;

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private SubmissionRepository submissionRepository;

	@Before
	public void init() {
		submission = getControllerEndpointSubmission();
		entityManager.persist(submission);
	}

	@Test
	public void testSubmission() {
		assert submissionRepository.findByHash(submission.getHash()).get(0).equals(submission);
	}

}