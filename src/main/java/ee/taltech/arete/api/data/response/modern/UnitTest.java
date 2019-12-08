package ee.taltech.arete.api.data.response.modern;

import lombok.*;

import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UnitTest {

	private UnitTestStatus status;
	private int weight;
	private String description;
	private boolean printExceptionMessage;
	private boolean printStackTrace;
	private int timeElapsed;
	private String name;
	private String stackTrace;
	private String exceptionClass;
	private String exceptionMessage;
	private List<StreamOutput> stdout;
	private List<StreamOutput> stderr;

	public enum UnitTestStatus {
		PASSED, FAILED, SKIPPED
	}

}
