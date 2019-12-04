package ee.taltech.arete.service.response;

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

	private static Logger LOGGER = LoggerFactory.getLogger(ReportService.class);
	@Autowired
	private JavaMailSender javaMailSender;

	@Override
	public void sendMail(String uniid, String resultPath) { // For results
		try {

			JSONObject json = new JSONObject(Files.readString(Paths.get(resultPath)));
			String mail = "Your submission received no results from tester.";

			JSONArray jsonArray = json.getJSONArray("results");

			for (int i = 0; i < jsonArray.length(); i++) {
				if (jsonArray.getJSONObject(i).get("identifier").equals("REPORT")) {
					mail = jsonArray.getJSONObject(i).get("output").toString();
				}
			}

			sendMailAsync(uniid, mail);

		} catch (IOException | JSONException e) {
			LOGGER.error(e.getMessage());
		}

	}

	@Override
	public void sendTextMail(String uniid, String text) { // For exceptions

		sendMailAsync(uniid, text);
	}

	private void sendMailAsync(String uniid, String text) {
//		Thread thread = new Thread(() -> {
			try {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setTo(String.format("%s@taltech.ee", uniid));
				message.setSubject("Test results");
				message.setText(text);
				javaMailSender.send(message);
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
			}
//		});
//		thread.setPriority(10);
//		thread.start();
	}

	@Override
	public void sendToReturnUrl(String returnUrl, String resultPath) {

		try {
			post(returnUrl, Files.readString(Paths.get(resultPath)));
		} catch (IOException e) {
			throw new RequestFormatException("Malformed returnUrl");
		}

	}

	@Override
	public void sendTextToReturnUrl(String returnUrl, String text) {

		String genericError = "{\n" +
				"  \"version\": \"2.0, build 20191126_122604\",\n" +
				"  \"type\": \"hodor_studenttester\",\n" +
				"  \"contentRoot\": \"/student\",\n" +
				"  \"testRoot\": \"/tester\",\n" +
				"  \"results\": [\n" +
				"    {\n" +
				"      \"code\": 2147483647,\n" +
				"      \"identifier\": \"REPORT\",\n" +
				"      \"output\": \"" + text + "\",\n" +
				"      \"result\": \"SUCCESS\"\n" +
				"    }\n" +
				"  ]\n" +
				"}";

		try {
			post(returnUrl, genericError);
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
