package ee.taltech.arete.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.RequestFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubmissionController {
	private static Logger LOGGER = LoggerFactory.getLogger(SubmissionController.class);

	@Autowired private ObjectMapper objectMapper;

	@PostMapping("/test")
	public ResponseEntity<Object> test(HttpEntity<String> httpEntity) {
		String requestBody = httpEntity.getBody();
		LOGGER.info("Parsing request body: " + requestBody);
		if (requestBody == null) throw new RequestFormatException("Empty input!");

		try {

			Submission submission = objectMapper.readValue(requestBody, Submission.class);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);

		} catch (JsonProcessingException e) {
			LOGGER.error("Request format invalid!", e);
			throw new RequestFormatException(e.getMessage(), e);

		}

	}

}
