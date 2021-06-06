package ee.taltech.arete_testing_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DockerTimeoutException extends RuntimeException {
    public DockerTimeoutException(String s) {
        super(s);
    }
}