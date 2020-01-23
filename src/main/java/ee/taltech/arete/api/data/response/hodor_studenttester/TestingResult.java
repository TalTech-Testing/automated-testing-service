package ee.taltech.arete.api.data.response.hodor_studenttester;
import lombok.*;

import java.util.ArrayList;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TestingResult {

	Integer code;
	Integer count;
	ArrayList<StyleError> errors;
	ArrayList<HodorFile> files;
	ArrayList<Diagnostic> diagnosticList;
	ArrayList<HodorTestContext> TestContexts;
	String identifier;
	String output;
	String result;
	Boolean securityViolation;
	Integer totalCount;
	String totalGrade; // Either Double or NaN
	Integer totalPassedCount;
}
