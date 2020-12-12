package ee.taltech.arete_testing_service.controller;

import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete.java.response.arete.SystemStateDTO;
import ee.taltech.arete_testing_service.Utils;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.service.RequestService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.List;

@RestController
@AllArgsConstructor
public class SubmissionController {

	private final RequestService requestService;

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping(path = {"/test", "/:testAsync"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public Submission Test(HttpEntity<String> httpEntity) {
		return requestService.testAsync(httpEntity);
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping(path = {"/test/sync", "/:testSync"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public AreteResponseDTO TestSync(HttpEntity<String> httpEntity) {
		return requestService.testSync(httpEntity);
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping(path = "/waitingroom/{hash}", produces = MediaType.APPLICATION_JSON_VALUE)
	public void WaitingList(HttpEntity<String> httpEntity, @PathVariable("hash") String hash) {
		requestService.waitingroom(httpEntity, hash);
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PutMapping(path = "/image/{image}", produces = MediaType.TEXT_PLAIN_VALUE)
	public String UpdateImage(@PathVariable("image") String image) {
		return requestService.updateImage(image);
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping(path = "/image/{image}:update", produces = MediaType.TEXT_PLAIN_VALUE)
	public String UpdateImageViaWebhook(@PathVariable("image") String image) {
		return requestService.updateImage(image);
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PutMapping(path = "/tests", produces = MediaType.TEXT_PLAIN_VALUE)
	public String UpdateTests(HttpEntity<String> httpEntity) {
		return requestService.updateTests(httpEntity);
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping(path = "/tests:update", produces = MediaType.TEXT_PLAIN_VALUE)
	public String UpdateTestsViaWebhook(HttpEntity<String> httpEntity) {
		return requestService.updateTests(httpEntity);
	}

	@SneakyThrows
	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping(path = "/submissions/active", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Submission> GetActiveSubmissions() {
		return requestService.getActiveSubmissions();
	}

	@SneakyThrows
	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping(path = "/logs", produces = MediaType.TEXT_PLAIN_VALUE)
	public String GetLogs() {
		return String.join("", Utils.tailFile(Paths.get("logs/spring.log"), 2000));
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping(path = "/state", produces = MediaType.APPLICATION_JSON_VALUE)
	public SystemStateDTO GetSystemState() {
		return new SystemStateDTO();
	}

}
