package ee.taltech.arete_testing_service.service;

import ee.taltech.arete_testing_service.AreteApplication;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.exception.RequestFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AreteApplication.class})
class SubmissionServiceTest {

	@Autowired
	private SubmissionService submissionService;

	@Test
	void populateDefaultValues() {
		Submission submission = Submission.empty();
		submission.setUniid("envomp");

		submissionService.populateDefaultValues(submission);

		assertTrue(submission.getHash().length() > 0);
		assertEquals(5, submission.getPriority());
		assertTrue(submission.getTimestamp() > 100000000);
		assertTrue(submission.getReceivedTimestamp() > 100000000);
		assertEquals(120, submission.getDockerTimeout());
		assertEquals("envomp@ttu.ee", submission.getEmail());
	}

	@Test
	void populateTesterRelatedFieldsThrowsException() {
		Submission submission = Submission.empty();

		Throwable thrown = catchThrowable(() -> submissionService.populateTesterRelatedFields(submission));

		assertThat(thrown).isInstanceOf(RequestFormatException.class);
	}

	@Test
	void populateTesterRelatedFieldsGit() {
		Submission submission = Submission.empty();
		submission.setGitTestRepo("https://gitlab.cs.ttu.ee/iti0102-2020/ex.git");

		submissionService.populateTesterRelatedFields(submission);

		assertEquals("iti0102-2020/ex", submission.getCourse());
	}

	@Test
	void populateTesterRelatedFieldsGitSsh() {
		Submission submission = Submission.empty();
		submission.setGitTestRepo("git@gitlab.cs.ttu.ee:iti0102-2020/ex.git");

		submissionService.populateTesterRelatedFields(submission);

		assertEquals("iti0102-2020/ex", submission.getCourse());
	}

	@Test
	void populateTesterRelatedFieldsSource() {
		Submission submission = Submission.empty();
		submission.getSystemExtra().add("skipCopyingTests");
		submission.setTestingPlatform("test");

		submissionService.populateTesterRelatedFields(submission);

		assertEquals("test", submission.getGitTestRepo());
	}

	@Test
	void populateStudentRelatedFieldsGit() {
		Submission submission = Submission.empty();
		submission.setGitStudentRepo("https://gitlab.cs.ttu.ee/envomp/iti0102-2020.git");

		submissionService.populateStudentRelatedFields(submission);

		assertEquals("envomp", submission.getUniid());
		assertEquals("iti0102-2020", submission.getFolder());
	}

	@Test
	void populateStudentRelatedFieldsGitSsh() {
		Submission submission = Submission.empty();
		submission.setGitStudentRepo("git@gitlab.cs.ttu.ee:envomp/iti0102-2020.git");

		submissionService.populateStudentRelatedFields(submission);

		assertEquals("envomp", submission.getUniid());
		assertEquals("iti0102-2020", submission.getFolder());
	}

	@Test
	void populateStudentRelatedFieldsSource() {
		Submission submission = Submission.empty();
		submission.getSystemExtra().add("skipCopyingStudent");
		submission.setTestingPlatform("test");

		submissionService.populateStudentRelatedFields(submission);

		assertEquals("test", submission.getGitTestRepo());
	}

	@Test
	void populateStudentRelatedFieldsThrowsException() {
		Submission submission = Submission.empty();

		Throwable thrown = catchThrowable(() -> submissionService.populateStudentRelatedFields(submission));

		assertThat(thrown).isInstanceOf(RequestFormatException.class);
	}

	@Test
	void populateSyncFieldsNoException() {
		Submission submission = Submission.empty();
		submission.setGitStudentRepo("git@gitlab.cs.ttu.ee:envomp/iti0102-2020.git");
		submission.setGitTestRepo("git@gitlab.cs.ttu.ee:iti0102-2020/ex.git");

		submissionService.populateSyncFields(submission);

		assertEquals("envomp", submission.getUniid());
	}

	@Test
	void populateAsyncFieldsNoException() {
		Submission submission = Submission.empty();
		submission.setGitStudentRepo("git@gitlab.cs.ttu.ee:envomp/iti0102-2020.git");
		submission.setGitTestRepo("git@gitlab.cs.ttu.ee:iti0102-2020/ex.git");

		submissionService.populateAsyncFields(submission);

		assertEquals("envomp", submission.getUniid());
	}
}