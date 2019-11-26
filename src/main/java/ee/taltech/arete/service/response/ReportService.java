package ee.taltech.arete.service.response;

import ee.taltech.arete.domain.Submission;


public interface ReportService {

	void sendMail(Submission submission, String resultPath);

	void sendTextMail(Submission submission, String text);

	void sendToReturnUrl(Submission submission, String resultPath);

}
