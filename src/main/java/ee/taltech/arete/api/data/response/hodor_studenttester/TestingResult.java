package ee.taltech.arete.api.data.response.hodor_studenttester;

import ee.taltech.arete.api.data.response.arete.TestContext;
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
	ArrayList<TestContext> testContexts;
	String identifier;
	String output;
	String result;
	Boolean securityViolation;
	Integer totalCount;
	String totalGrade; // Either int or NaN
	Integer totalPassedCount;
}
