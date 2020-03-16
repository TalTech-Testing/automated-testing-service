package ee.taltech.arete.service.response;

public interface ReportService {

	void sendTextMail(String uniid, String text, String header, Boolean html);

	void sendTextToReturnUrl(String returnUrl, String response);

}
