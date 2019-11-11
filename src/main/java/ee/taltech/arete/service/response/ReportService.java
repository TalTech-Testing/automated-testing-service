package ee.taltech.arete.service.response;

import ee.taltech.arete.domain.Submission;

import java.io.IOException;


public interface ReportService {

	void sendMail(Submission submission);

	void sendToReturnUrl(Submission submission);

}
