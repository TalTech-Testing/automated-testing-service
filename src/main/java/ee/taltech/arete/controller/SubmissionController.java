package ee.taltech.arete.controller;

import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.RequestFormatException;
import ee.taltech.arete.service.queue.PriorityQueueService;
import ee.taltech.arete.service.request.RequestService;
import ee.taltech.arete.service.submission.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SubmissionController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private PriorityQueueService priorityQueueService;

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


    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/submissions/active")
    public List<Submission> GetActiveSubmissions() {

        try {
            return priorityQueueService.getActiveSubmissions();
        } catch (Exception e) {
            return new ArrayList<>();
        }

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/debug/{bool}")
    public void setDebug(@PathVariable("bool") int bool) {

        try {
            submissionService.debugMode(bool != 0);
        } catch (Exception e) {
            throw new RequestFormatException(e.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/logs")
    public String GetLogs() {

        try {
            return Files.readString(Paths.get("logs/spring.log"));
        } catch (Exception e) {
            throw new RequestFormatException(e.getMessage());
        }
    }
}
