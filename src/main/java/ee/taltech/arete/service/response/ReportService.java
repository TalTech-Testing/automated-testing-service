package ee.taltech.arete.service.response;

import java.util.Optional;

public interface ReportService {

	void sendTextMail(String eMail, String text, String header, Boolean html, Optional<String> files);

	void sendTextToReturnUrl(String returnUrl, String response);

}
