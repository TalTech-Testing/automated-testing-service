package ee.taltech.arete.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DockerRunnerException extends RuntimeException {
	public DockerRunnerException(String s) {
		super(s);
	}
}