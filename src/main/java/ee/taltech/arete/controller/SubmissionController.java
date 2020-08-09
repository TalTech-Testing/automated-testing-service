package ee.taltech.arete.controller;

import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.api.data.response.arete.SystemState;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.RequestFormatException;
import ee.taltech.arete.service.PriorityQueueService;
import ee.taltech.arete.service.RequestService;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RestController
public class SubmissionController {

	private final RequestService requestService;
	private final PriorityQueueService priorityQueueService;

	public SubmissionController(RequestService requestService, PriorityQueueService priorityQueueService) {
		this.requestService = requestService;
		this.priorityQueueService = priorityQueueService;
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping({"/test", "/:testAsync"})
	public Submission Test(HttpEntity<String> httpEntity) {
		return requestService.testAsync(httpEntity);
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping({"/test/sync", "/:testSync"})
	public ResponseEntity<AreteResponse> TestSync(HttpEntity<String> httpEntity) {
		try {
			AreteResponse response = requestService.testSync(httpEntity);
			if (!response.getFailed()) {
				return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
			} else {
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(new AreteResponse("NaN", new Submission(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/waitingroom/{hash}")
	public void WaitingList(HttpEntity<String> httpEntity, @PathVariable("hash") String hash) {

		requestService.waitingroom(httpEntity, hash);

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PutMapping("/image/{image}")
	public String UpdateImage(@PathVariable("image") String image) {
		return requestService.updateImage(image);
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/image/{image}:update")
	public String UpdateImageViaWebhook(@PathVariable("image") String image) {
		return requestService.updateImage(image);
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PutMapping("/tests")
	public String UpdateTests(HttpEntity<String> httpEntity) {
		return requestService.updateTests(httpEntity);
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/tests:update")
	public String UpdateTestsViaWebhook(HttpEntity<String> httpEntity) {
		return requestService.updateTests(httpEntity);
	}

	@SneakyThrows
	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/submissions/active")
	public List<Submission> GetActiveSubmissions() {
		return priorityQueueService.getActiveSubmissions();
	}

	public static List<String> tailFile(final Path source, final int noOfLines) throws IOException {
		try (Stream<String> stream = Files.lines(source)) {
			FileBuffer fileBuffer = new FileBuffer(noOfLines);
			stream.forEach(fileBuffer::collect);
			return fileBuffer.getLines();
		}
	}

	@SneakyThrows
	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/logs")
	public String GetLogs() {
		return String.join("", tailFile(Paths.get("logs/spring.log"), 2000));

	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/state")
	public SystemState GetSystemState() {
		try {
			return new SystemState();
		} catch (Exception e) {
			throw new RequestFormatException(e.getMessage());
		}
	}

	private static final class FileBuffer {
		private final int noOfLines;
		private final String[] lines;
		private int offset = 0;

		public FileBuffer(int noOfLines) {
			this.noOfLines = noOfLines;
			this.lines = new String[noOfLines];
		}

		public void collect(String line) {
			lines[offset++ % noOfLines] = line;
		}

		public List<String> getLines() {
			return IntStream.range(offset < noOfLines ? 0 : offset - noOfLines, offset)
					.mapToObj(idx -> lines[idx % noOfLines]).collect(Collectors.toList());
		}
	}

}
