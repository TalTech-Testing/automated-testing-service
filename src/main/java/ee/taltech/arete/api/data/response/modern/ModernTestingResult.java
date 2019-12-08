package ee.taltech.arete.api.data.response.modern;

import ee.taltech.arete.api.data.response.TestingResult;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor
public class ModernTestingResult extends TestingResult<ModernTestingResultDetails> {

	private String contentRoot;
	private String testRoot;

	@Builder
	private ModernTestingResult(String type,
	                            String version,
	                            String stdout,
	                            String stderr,
	                            String output,
	                            String contentRoot,
	                            String testRoot,
	                            List<ModernTestingResultDetails> results) {
		super(type, version, stdout, stderr, output, results);
		this.contentRoot = contentRoot;
		this.testRoot = testRoot;
	}

}
