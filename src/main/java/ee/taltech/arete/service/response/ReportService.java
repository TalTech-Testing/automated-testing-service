package ee.taltech.arete.service.response;

public interface ReportService {

	void sendMail(String uniid, String resultPath);

	void sendTextMail(String uniid, String text);

	void sendToReturnUrl(String returnUrl, String resultPath);

	void sendTextToReturnUrl(String returnUrl, String text);

}
