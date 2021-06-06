package ee.taltech.arete_testing_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RequestFormatException extends RuntimeException {
    public RequestFormatException(String s) {
        super(s);
    }

    public RequestFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
