package ee.taltech.arete.service.response;

import ee.taltech.arete.api.data.response.arete.AreteResponse;

public interface ReportService {

	void sendTextMail(String uniid, String text, String header, Boolean html);

	void sendTextToReturnUrl(String returnUrl, AreteResponse response);

}
