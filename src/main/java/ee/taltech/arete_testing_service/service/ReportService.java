package ee.taltech.arete_testing_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete.java.response.hodor_studenttester.HodorStudentTesterResponse;
import ee.taltech.arete_testing_service.configuration.ServerConfiguration;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.service.arete.AreteConstructor;
import ee.taltech.arete_testing_service.service.hodor.HodorParser;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ReportService {

	private final Logger logger;
	private final ServerConfiguration serverConfiguration;
	private final JavaMailSender javaMailSender;
	private final ObjectMapper objectMapper;

	public AreteResponseDTO getAreteResponse(String json) throws JsonProcessingException {
		AreteResponseDTO responseDTO = objectMapper.readValue(json, AreteResponseDTO.class);
		responseDTO.setType("arete");
		responseDTO.setVersion("2.1");
		return responseDTO;
	}

	public void reportSuccessfulSubmission(String slug, Submission submission, String outputPath) {

		AreteResponseDTO areteResponse; // Sent to Moodle
		String message; // Sent to student
		boolean html = false;

		try {
			String json = Files.readString(Paths.get(outputPath + "/output.json"), StandardCharsets.UTF_8);
			JSONObject jsonObject = new JSONObject(json);

			try {
				if ("hodor_studenttester".equals(jsonObject.get("type"))) {
					html = true;
					HodorStudentTesterResponse response = objectMapper.readValue(json, HodorStudentTesterResponse.class);
					areteResponse = HodorParser.parse(response);
					AreteConstructor.fillFromSubmission(slug, submission, areteResponse);

				} else if ("arete".equals(jsonObject.get("type"))) {
					html = true;
					areteResponse = getAreteResponse(json);
					AreteConstructor.fillFromSubmission(slug, submission, areteResponse);

				} else {
					areteResponse = AreteConstructor.failedSubmission(slug, submission, "Unsupported tester type.");
				}
			} catch (Exception e1) {
				html = false;
				logger.error("Failed constructing areteResponse: {}", e1.getMessage());
				if (jsonObject.has("output") && jsonObject.get("output") != null) {
					areteResponse = AreteConstructor.failedSubmission(slug, submission, jsonObject.get("output").toString());
				} else {
					message = "Error occurred when reading test results from TestRunner created output. This is most likely due to invalid runtime configuration, that resulted in tester not giving a result.";
					areteResponse = AreteConstructor.failedSubmission(slug, submission, message);
				}
			}

			message = areteResponse.getOutput();

		} catch (Exception e) {
			logger.error("Generating a failed response: {}", e.getMessage());
			message = "Error occurred when reading test results from TestRunner created output. This is most likely due to invalid runtime configuration, that resulted in tester not giving a result.";
			areteResponse = AreteConstructor.failedSubmission(slug, submission, message);
		}

		this.reportSubmission(submission, areteResponse, message, slug, html, Optional.of(outputPath));

	}

	public void reportFailedSubmission(Submission submission, String errorMessage) {
		String message = String.format("Testing failed with message: %s", errorMessage);
		AreteResponseDTO areteResponse;
		if (submission.getSlugs() == null) {
			areteResponse = AreteConstructor.failedSubmission("undefined", submission, message);
		} else {
			areteResponse = AreteConstructor.failedSubmission(submission.getSlugs().stream().findFirst().orElse("undefined"), submission, message);
		}

		areteResponse.setConsoleOutputs(areteResponse.getConsoleOutputs() + "\n" + message);
		this.reportSubmission(submission, areteResponse, message, "Failed submission", false, Optional.empty());
	}

	@SneakyThrows
	private void reportSubmission(Submission submission, AreteResponseDTO areteResponse, String message, String header, Boolean html, Optional<String> output) {

		if (submission.getSystemExtra().contains("integration_tests")) {
			returnToIntegrationTest(submission, areteResponse, header, html, output);
			areteResponse.setUniid("integration-test");
			reportToBackend(submission, areteResponse);
			return;
		}

		reportToReturnUrl(submission, areteResponse);
		reportToBackend(submission, areteResponse);
		reportToStudent(submission, areteResponse, message, header, html, output);
		reportToTeacher(submission, areteResponse, header, html, output);
	}

	private void returnToIntegrationTest(Submission submission, AreteResponseDTO areteResponse, String header, Boolean isHtml, Optional<String> output) throws JsonProcessingException {
		this.sendTextToReturnUrl(submission.getReturnUrl(), objectMapper.writeValueAsString(areteResponse), serverConfiguration.getAreteBackendToken());
		logger.info("INTEGRATION TEST: Reported to return url for {} with score {}%", submission.getUniid(), areteResponse.getTotalGrade());

		String integrationTestMail = System.getenv("INTEGRATION_TEST_MAIL");
		if (integrationTestMail != null) {
			this.sendTextMail(integrationTestMail, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission), header, isHtml, output);
		}
	}

	private void reportToReturnUrl(Submission submission, AreteResponseDTO areteResponse) {
		try {
			if (submission.getReturnUrl() != null) {
				this.sendTextToReturnUrl(submission.getReturnUrl(), objectMapper.writeValueAsString(areteResponse), "migrate to use auth header pls");
				logger.info("Reported to return url for {} with score {}%", submission.getUniid(), areteResponse.getTotalGrade());
			}
		} catch (Exception e1) {
			logger.error("Malformed returnUrl: {}", e1.getMessage());
		}
	}

	private void reportToBackend(Submission submission, AreteResponseDTO areteResponse) {
		JsonNode node = submission.getReturnExtra();
		try {

			if (submission.getSystemExtra().contains("anonymous")) {
				areteResponse.setReturnExtra(null);
			}

			JSONObject extra = new JSONObject();
			extra.put("used_extra", areteResponse.getReturnExtra());
			areteResponse.setReturnExtra(new ObjectMapper().readTree(extra.toString()));

			this.sendTextToReturnUrl(serverConfiguration.getAreteBackend(), objectMapper.writeValueAsString(areteResponse), serverConfiguration.getAreteBackendToken());
			logger.info("Reported to backend");
		} catch (Exception e1) {
			logger.error("Failed to report to backend with message: {}", e1.getMessage());
		} finally {
			areteResponse.setReturnExtra(node);
		}
	}

	private void reportToTeacher(Submission submission, AreteResponseDTO areteResponse, String header, Boolean html, Optional<String> output) {
		try {
			if (areteResponse.getFailed()) {
				String submissionString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(submission);
				try {
					this.sendTextMail(serverConfiguration.getAgo(), submissionString, header, html, output);
					if (!serverConfiguration.getAgo().equals(serverConfiguration.getDeveloper())) {
						this.sendTextMail(serverConfiguration.getDeveloper(), submissionString, header, html, output);
					}
				} catch (Exception e) {
					this.sendTextMail(serverConfiguration.getAgo(), submissionString, header, html, Optional.empty());
					if (!serverConfiguration.getAgo().equals(serverConfiguration.getDeveloper())) {
						this.sendTextMail(serverConfiguration.getDeveloper(), submissionString, header, html, Optional.empty());
					}
				}
			}

		} catch (Exception e1) {
			logger.error("Malformed mail: {}", e1.getMessage());
		}
	}

	private void reportToStudent(Submission submission, AreteResponseDTO areteResponse, String message, String header, Boolean html, Optional<String> output) {
		if (!submission.getSystemExtra().contains("noMail")) {
			try {
				this.sendTextMail(submission.getEmail(), message, header, html, output);
				logger.info("Reported to {} mailbox", submission.getEmail());
			} catch (Exception e1) {
				logger.error("Malformed mail: {}", e1.getMessage());
				areteResponse.setFailed(true);
				submission.setResult(submission.getResult() + "\n\n\n" + e1.getMessage());
			}
		}
	}

	private void sendTextMail(String mail, String text, String header, Boolean html, Optional<String> files) {

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
			helper.setFrom(serverConfiguration.getAreteMail());
			helper.setTo(mail);
			helper.setSubject(header);
			helper.setText(text, html);
			if (files.isPresent()) {
				for (File file : FileUtils.listFiles(Paths.get(files.get()).toFile(), new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY)) {
					if (!file.getName().equals("input.json") && !file.getName().equals("output.json")) {
						try {
							helper.addAttachment(file.getName(), file);
						} catch (Exception e) {
							logger.warn("Failed attaching file: {}", e.getMessage());
						}
					}
				}
			}
			javaMailSender.send(message);
		} catch (Exception e) {
			logger.error("Failed sending mail: {}", e.getMessage());
		}
	}

	private void sendTextToReturnUrl(String returnUrl, String response, String token) {
		try {
			post(returnUrl, response, token);
		} catch (IOException | InterruptedException e) {
			logger.error("Failed to POST: {}", e.getMessage());
		}

	}

	private void post(String postUrl, String data, String token) throws IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(postUrl))
				.POST(HttpRequest.BodyPublishers.ofString(data))
				.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.setHeader(HttpHeaders.AUTHORIZATION, token)
				.build();

		client.send(request, HttpResponse.BodyHandlers.ofString());

	}
}
