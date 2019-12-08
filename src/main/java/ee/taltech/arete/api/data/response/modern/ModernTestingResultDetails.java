package ee.taltech.arete.api.data.response.modern;

import ee.taltech.arete.api.data.SourceFile;
import ee.taltech.arete.api.data.response.TestingResultDetails;
import lombok.*;

import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ModernTestingResultDetails extends TestingResultDetails {

	private int code;
	private String identifier;
	private String result;
	private List<SourceFile> files;
	private List<TestContext> testContexts;
	private List<Diagnostic> diagnosticsList;
	private Integer totalCount;
	private Integer totalPassedCount;
	private Integer totalGrade;
	private Boolean securityViolation;
	private String output;

}
