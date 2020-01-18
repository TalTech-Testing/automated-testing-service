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
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SubmissionController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private PriorityQueueService priorityQueueService;

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping({"/test", ":testAsync"})
    public Submission Test(HttpEntity<String> httpEntity) {
        return requestService.testAsync(httpEntity);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping({"/test/sync", ":testSync"})
    public AreteResponse TestSync(HttpEntity<String> httpEntity) {

        return requestService.testSync(httpEntity);

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/waitingroom/{hash}")
    public void WaitingList(HttpEntity<String> httpEntity, @PathVariable("hash") String hash) {
        requestService.waitingroom(httpEntity, hash);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/image/{image}")
    public String UpdateImageViaWebhook(@PathVariable("image") String image) {

        return requestService.updateImage(image);

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/image/{image}:update")
    public String UpdateImage(@PathVariable("image") String image) {

        return requestService.updateImage(image);

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/tests")
    public String UpdateTestsViaWebhook(HttpEntity<String> httpEntity) {

        return requestService.updateTests(httpEntity);

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/tests:update")
    public String UpdateTests(HttpEntity<String> httpEntity) {

        return requestService.updateTests(httpEntity);

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/submission/{hash}")
    public List<Submission> GetSubmissionsByHash(@PathVariable("hash") String hash) {

        try {
            return submissionService.getSubmissionByHash(hash);
        } catch (Exception e) {
            return new ArrayList<>();
        }

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/submissions")
    public List<Submission> GetSubmissions() {

        try {

            List<Submission> submissions = submissionService.getSubmissions()
                    .stream()
                    .sorted(Comparator.comparingLong(Submission::getTimestamp))
                    .collect(Collectors.toList());
            int n = Math.min(20, submissions.size());
            return submissions.subList(submissions.size() - n, submissions.size());

        } catch (Exception e) {
            return new ArrayList<>();
        }

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/submissions/{n}")
    public List<Submission> GetNSubmissions(@PathVariable("n") Integer n) {

        try {
            List<Submission> submissions = submissionService.getSubmissions()
                    .stream()
                    .sorted(Comparator.comparingLong(Submission::getTimestamp))
                    .collect(Collectors.toList());
            n = Math.min(n, submissions.size());
            return submissions.subList(submissions.size() - n, submissions.size());

        } catch (Exception e) {
            return new ArrayList<>();
        }

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
    @GetMapping("/submission/{hash}/logs")
    public List<List<AreteResponse>> GetSubmissionLogs(@PathVariable("hash") String hash) {

        try {
            return submissionService.getSubmissionByHash(hash)
                    .stream()
                    .map(Submission::getResponse)
                    .collect(Collectors.toList());
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
