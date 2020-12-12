package ee.taltech.arete_testing_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.java.response.arete.FileDTO;
import ee.taltech.arete_testing_service.configuration.DevProperties;
import ee.taltech.arete_testing_service.domain.OverrideParameters;
import ee.taltech.arete_testing_service.domain.OverrideParametersCollection;
import ee.taltech.arete_testing_service.domain.Submission;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

/**
 * All given functions modify submission in place
 */
@Service
@AllArgsConstructor
public class SubmissionPropertyService {

	private final Logger logger;
	private final ObjectMapper objectMapper;
	private final DevProperties devProperties;


	public OverrideParametersCollection update(Submission submission, String slug, String initialEmail) {
		Optional<OverrideParameters> testRoot = this.rootProperties(submission);
		Optional<OverrideParameters> testGroup = this.groupingFolderProperties(submission, slug);
		Optional<OverrideParameters> testSlug = this.slugProperties(submission, slug);

		Optional<OverrideParameters> studentRoot = this.studentRootProperties(submission);
		Optional<OverrideParameters> studentGroup = this.studentGroupingFolderProperties(submission, slug);
		Optional<OverrideParameters> studentSlug = this.studentSlugProperties(submission, slug);

		this.modifyEmail(submission, initialEmail);
		this.readTesterFiles(submission, slug);
		this.readStudentFiles(submission, slug);

		boolean[] changed = this.fillMissingValues(submission);

		return OverrideParametersCollection.builder()
				.changed(changed)
				.studentGroup(studentGroup)
				.studentRoot(studentRoot)
				.studentSlug(studentSlug)
				.testGroup(testGroup)
				.testRoot(testRoot)
				.testSlug(testSlug)
				.build();

	}

	public void revert(Submission submission, OverrideParametersCollection params) {
		revertAddedDefaults(submission, params.getChanged());

		params.getStudentSlug().ifPresent(x -> x.revert(submission));
		params.getStudentGroup().ifPresent(x -> x.revert(submission));
		params.getStudentRoot().ifPresent(x -> x.revert(submission));

		params.getTestSlug().ifPresent(x -> x.revert(submission));
		params.getTestGroup().ifPresent(x -> x.revert(submission));
		params.getTestRoot().ifPresent(x -> x.revert(submission));
	}

	private void revertAddedDefaults(Submission submission, boolean[] changed) {
		if (changed[0]) {
			submission.setDockerContentRoot(null);
		}

		if (changed[1]) {
			submission.setDockerExtra(null);
		}

		if (changed[2]) {
			submission.setDockerTestRoot(null);
		}
	}

	private boolean[] fillMissingValues(Submission submission) {
		boolean[] modified = new boolean[]{false, false, false};

		if (submission.getDockerContentRoot() == null) {
			submission.setDockerContentRoot("/student");
			modified[0] = true;
		}

		if (submission.getDockerExtra() == null) {
			submission.setDockerExtra("");
			modified[1] = true;
		}

		if (submission.getDockerTestRoot() == null) {
			submission.setDockerTestRoot("/tester");
			modified[2] = true;
		}

		return modified;
	}

	private void readStudentFiles(Submission submission, String slug) {
		try {
			if (submission.getGitStudentRepo() != null && submission.getGitStudentRepo().contains("git") && submission.getSource() == null) {
				String student = String.format("students/%s/%s/%s", submission.getUniid(), submission.getFolder(), slug);

				FileUtils.listFiles(
						new File(student),
						new RegexFileFilter("^(.*?)"),
						DirectoryFileFilter.DIRECTORY
				).parallelStream().sequential()
						.forEach(x -> {
							try {
								if (submission.getSource() == null) {
									submission.setSource(new ArrayList<>());
								}
								submission.getSource()
										.add(FileDTO.builder()
												.contents(Files.readString(x.toPath(), StandardCharsets.UTF_8))
												.path(x.getPath())
												.build());
							} catch (IOException e) {
								submission.setSource(null);
							}
						});
			}
		} catch (Exception e) {
			submission.setSource(null);
		}
	}

	private void readTesterFiles(Submission submission, String slug) {
		try {
			if (submission.getGitTestRepo() != null && submission.getGitTestRepo().contains("git") && submission.getTestSource() == null) {
				String tester = String.format("tests/%s/%s", submission.getCourse(), slug);

				FileUtils.listFiles(
						new File(tester),
						new RegexFileFilter("^(.*?)"),
						DirectoryFileFilter.DIRECTORY
				).parallelStream().sequential()
						.forEach(x -> {
							try {
								if (submission.getTestSource() == null) {
									submission.setTestSource(new ArrayList<>());
								}
								submission.getTestSource()
										.add(FileDTO.builder()
												.contents(Files.readString(x.toPath(), StandardCharsets.UTF_8))
												.path(x.getPath())
												.build());
							} catch (IOException e) {
								submission.setTestSource(null);
							}
						});
			}
		} catch (Exception e) {
			submission.setTestSource(null);
		}
	}

	public void modifyEmail(Submission submission, String initialEmail) {
		submission.setEmail(initialEmail);

		if (!submission.getSystemExtra().contains("allowExternalMail")) {
			if (!submission.getEmail().matches(devProperties.getSchoolMailMatcher())) {
				submission.setEmail(submission.getUniid() + "@ttu.ee");
			}
		}
	}

	public void formatSlugs(Submission submission) {
		HashSet<String> formattedSlugs = new HashSet<>();

		for (String changed_file : submission.getSlugs()) {
			String potentialSlug = changed_file.split("[/\\\\]")[0];
			if (potentialSlug.matches(devProperties.getNameMatcher())) {
				if (submission.getGroupingFolders().contains(potentialSlug)) {
					try {
						String innerPotentialSlug = changed_file.split("[/\\\\]")[1];
						if (innerPotentialSlug.matches(devProperties.getNameMatcher())) {
							formattedSlugs.add(potentialSlug + "/" + innerPotentialSlug);
						}
					} catch (Exception ignored) {
					}
				} else {
					formattedSlugs.add(potentialSlug);
				}
			}
		}

		submission.setSlugs(formattedSlugs);
	}

	Optional<OverrideParameters> rootProperties(Submission submission) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				String path = String.format("tests/%s/arete.json", submission.getCourse());
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParameters(submission);
				logger.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				logger.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}

	private Optional<OverrideParameters> groupingFolderProperties(Submission submission, String slug) {
		Optional<String> group = extractGroupFromSlug(slug);
		if (!submission.getSystemExtra().contains("noOverride") && group.isPresent()) {
			try {
				String path = String.format("tests/%s/%s/arete.json", submission.getCourse(), group.get());
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParameters(submission);
				logger.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				logger.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}

	private Optional<OverrideParameters> slugProperties(Submission submission, String slug) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				String path = String.format("tests/%s/%s/arete.json", submission.getCourse(), slug);
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParameters(submission);
				logger.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				logger.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}

	private Optional<OverrideParameters> studentRootProperties(Submission submission) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				String path = String.format("students/%s/%s/arete.json", submission.getUniid(), submission.getFolder());
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParametersForStudent(submission);
				logger.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				logger.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}

	private Optional<OverrideParameters> studentGroupingFolderProperties(Submission submission, String slug) {
		Optional<String> group = extractGroupFromSlug(slug);
		if (!submission.getSystemExtra().contains("noOverride") && group.isPresent()) {
			try {
				String path = String.format("students/%s/%s/%s/arete.json", submission.getUniid(), submission.getFolder(), group.get());
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParametersForStudent(submission);
				logger.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				logger.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}

	private Optional<OverrideParameters> studentSlugProperties(Submission submission, String slug) {
		if (!submission.getSystemExtra().contains("noOverride")) {
			try {
				String path = String.format("students/%s/%s/%s/arete.json", submission.getUniid(), submission.getFolder(), slug);
				OverrideParameters params = objectMapper.readValue(new File(path), OverrideParameters.class);
				params.invoke(submission);
				params.overrideParametersForStudent(submission);
				logger.info("Overrode default parameters: {}", params);
				return Optional.of(params);
			} catch (Exception e) {
				logger.info("Using default parameters: {}", e.getMessage());
			}
		}
		return Optional.empty();
	}

	private Optional<String> extractGroupFromSlug(String slug) {
		String[] groups = slug.split("[/\\\\]");
		if (groups.length > 1) {
			return Optional.of(groups[0]);
		}
		return Optional.empty();
	}

}
