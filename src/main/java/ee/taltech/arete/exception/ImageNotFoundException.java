package ee.taltech.arete.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ImageNotFoundException extends RuntimeException {
	public ImageNotFoundException(String s) {
		super(s);
	}

	public ImageNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}