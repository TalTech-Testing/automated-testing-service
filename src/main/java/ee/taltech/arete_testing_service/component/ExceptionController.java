package ee.taltech.arete_testing_service.component;

import ee.taltech.arete_testing_service.exception.ImageNotFoundException;
import ee.taltech.arete_testing_service.exception.RequestFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


@ControllerAdvice
public class ExceptionController extends ResponseEntityExceptionHandler  {

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = ImageNotFoundException.class)
	public ResponseEntity<Object> exception(ImageNotFoundException exception) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("message", exception.getMessage());
		return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = RequestFormatException.class)
	public ResponseEntity<Object> exception(RequestFormatException exception) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("message", exception.getMessage());
		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

}
