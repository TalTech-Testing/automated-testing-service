package ee.taltech.arete.service.response;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.RequestFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class ReportServiceImpl implements ReportService {

	@Autowired
	private JavaMailSender javaMailSender;

	private static Logger LOGGER = LoggerFactory.getLogger(ReportService.class);

	@Override
	public void sendMail(Submission submission, String resultPath) {
		try {

			JSONObject json = new JSONObject(Files.readString(Paths.get(resultPath)));
			String mail = "Your last commit/push did not include any project folders (e.g. EX01, HW01 etc.)\n" +
					"You can try to change something in the code and commit/push again.";

			JSONArray jsonArray = json.getJSONArray("results");

			for (int i = 0; i < jsonArray.length(); i++) {
				if (jsonArray.getJSONObject(i).get("identifier").equals("REPORT")) {
					mail = jsonArray.getJSONObject(i).get("output").toString();
				}
			}

			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(String.format("%s@taltech.ee", submission.getUniid()));
			message.setSubject("Test results");
			message.setText(mail);
			javaMailSender.send(message);

		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void sendToReturnUrl(Submission submission, String resultPath) {

		try {
			post(submission.getReturnUrl(), Files.readString(Paths.get(resultPath)));
		} catch (IOException e) {
			throw new RequestFormatException("Malformed returnUrl");
		}

	}

	private void post(String postUrl, String data) throws IOException {
		URL url = new URL(postUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestProperty("Content-Type", "application/json");
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
