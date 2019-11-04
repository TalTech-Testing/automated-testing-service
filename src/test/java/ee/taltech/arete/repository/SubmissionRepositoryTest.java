package ee.taltech.arete.repository;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.service.submission.SubmissionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@DataJpaTest
public class SubmissionRepositoryTest {

	private Submission submission;

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private SubmissionRepository submissionRepository;

	@Before
	public void init() {
		submission = new Submission("envomp", "hash", "python", "neti.ee", new String[]{"style"});
		entityManager.persist(submission);
	}

	@Test
	public void testSubmission() {
		System.out.println(submission);
		assert submissionRepository.findByHash("hash").get(0).equals(submission);
	}

}