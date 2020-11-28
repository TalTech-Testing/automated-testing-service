package ee.taltech.arete_testing_service.service;

import ee.taltech.arete_testing_service.configuration.DevProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.Optional;

@EnableAsync
@Service
public class ReportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);

	final DevProperties devProperties;

	private final JavaMailSender javaMailSender;

	public ReportService(JavaMailSender javaMailSender, DevProperties devProperties) {
		this.javaMailSender = javaMailSender;
		this.devProperties = devProperties;
	}

	public void sendTextMail(String mail, String text, String header, Boolean html, Optional<String> files) {

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
			helper.setFrom(devProperties.getAreteMail());
			helper.setTo(mail);
			helper.setSubject(header);
			helper.setText(text, html);
			if (files.isPresent()) {
				for (File file : FileUtils.listFiles(Paths.get(files.get()).toFile(), new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY)) {
					if (!file.getName().equals("input.json") && !file.getName().equals("output.json")) {
						try {
							helper.addAttachment(file.getName(), file);
						} catch (Exception e) {
							LOGGER.warn("Failed attaching file: {}", e.getMessage());
						}
					}
				}
			}
			javaMailSender.send(message);
		} catch (Exception e) {
			LOGGER.error("Failed sending mail: {}", e.getMessage());
		}
	}

	@Async
	public void sendTextToReturnUrl(String returnUrl, String response) {
		try {
			post(returnUrl, response);
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Failed to POST: {}", e.getMessage());
		}

	}

	private void post(String postUrl, String data) throws IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(postUrl))
				.POST(HttpRequest.BodyPublishers.ofString(data))
				.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.build();

		client.send(request, HttpResponse.BodyHandlers.ofString());

	}
}
