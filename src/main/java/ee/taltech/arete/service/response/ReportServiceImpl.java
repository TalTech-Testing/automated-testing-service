package ee.taltech.arete.service.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.exception.RequestFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@EnableAsync
@Service
public class ReportServiceImpl implements ReportService {

	private static Logger LOGGER = LoggerFactory.getLogger(ReportService.class);

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private ObjectMapper objectMapper;

	@Async
	@Override
	public void sendTextMail(String uniid, String text, String header, Boolean html) {

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
			helper.setFrom("automatedTestingService@taltech.ee");
			helper.setTo(String.format("%s@taltech.ee", uniid));
			helper.setSubject(header);
			helper.setText(text, html);
			javaMailSender.send(message);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	@Override
	public void sendTextToReturnUrl(String returnUrl, AreteResponse response) {

		try {
			post(returnUrl, objectMapper.writeValueAsString(response));
		} catch (IOException | InterruptedException e) {
			throw new RequestFormatException("Malformed returnUrl");
		}
	}

	private void post(String postUrl, String data) throws IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(postUrl))
				.POST(HttpRequest.BodyPublishers.ofString(data))
				.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.build();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());

	}
}
