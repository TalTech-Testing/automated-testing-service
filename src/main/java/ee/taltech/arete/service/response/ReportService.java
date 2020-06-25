package ee.taltech.arete.service.response;

public interface ReportService {

	void sendTextMail(String eMail, String text, String header, Boolean html);

	void sendTextToReturnUrl(String returnUrl, String response);

}
