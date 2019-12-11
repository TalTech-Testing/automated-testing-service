package ee.taltech.arete.service.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.exception.RequestFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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
	public void sendTextMail(String uniid, String text) { // For exceptions

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(String.format("%s@taltech.ee", uniid));
			message.setSubject("Test results");
			message.setText(text);
			javaMailSender.send(message);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	@Override
	public void sendTextToReturnUrl(String returnUrl, AreteResponse response) {

		try {
			post(returnUrl, objectMapper.writeValueAsString(response));
		} catch (IOException e) {
			throw new RequestFormatException("Malformed returnUrl");
		}
	}

	private void post(String postUrl, String data) throws IOException {
		URL url = new URL(postUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		con.setDoOutput(true);

		this.sendData(con, data);
		this.read(con.getInputStream());
	}

	private void sendData(HttpURLConnection con, String data) throws IOException {
		DataOutputStream wr = null;
		try {
			wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(data);
			wr.flush();
			wr.close();
		} finally {
			this.closeQuietly(wr);
		}
	}

	private void read(InputStream is) throws IOException {
		BufferedReader in = null;
		String inputLine;
		StringBuilder body;
		try {
			in = new BufferedReader(new InputStreamReader(is));

			body = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				body.append(inputLine);
			}
			in.close();

		} finally {
			this.closeQuietly(in);
		}
	}

	private void closeQuietly(Closeable closeable) throws IOException {
		if (closeable != null) {
			closeable.close();
		}
	}
}
