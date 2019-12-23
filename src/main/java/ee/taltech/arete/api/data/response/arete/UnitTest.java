package ee.taltech.arete.api.data.response.arete;

import ee.taltech.arete.api.data.response.hodor_studenttester.TesterStderr;
import ee.taltech.arete.api.data.response.hodor_studenttester.TesterStdout;
import lombok.*;

import java.util.ArrayList;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UnitTest {
	String status;
	Integer weight;
	String description;
	Boolean printExceptionMessage = true;
	Boolean printStackTrace = true;
	Long timeElapsed;
	ArrayList<String> groupsDependedUpon;
	ArrayList<String> methodsDependedUpon;
	String name;
	String stackTrace;
	String exceptionClass;
	String exceptionMessage;
	ArrayList<TesterStdout> stdout;
	ArrayList<TesterStderr> stderr;

}
