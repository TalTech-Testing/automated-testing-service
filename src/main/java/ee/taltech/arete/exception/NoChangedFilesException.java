package ee.taltech.arete.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoChangedFilesException extends RuntimeException {
	public NoChangedFilesException(String s) {
		super(s);
	}

	public NoChangedFilesException(String message, Throwable cause) {
		super(message, cause);
	}
}
