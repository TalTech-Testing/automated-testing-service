package ee.taltech.arete.api.data.response.legacy;

import ee.taltech.arete.api.data.SourceFile;
import ee.taltech.arete.api.data.response.TestingResult;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor
public class LegacyTestingResult extends TestingResult<LegacyTestingResultDetails> {

	private int percent;
	private String extra;
	private List<SourceFile> files;

	@Builder
	private LegacyTestingResult(String type,
	                            String version,
	                            String stdout,
	                            String stderr,
	                            String output,
	                            String extra,
	                            int percent,
	                            List<LegacyTestingResultDetails> results,
	                            List<SourceFile> files) {
		super(type, version, stdout, stderr, output, results);
		this.percent = percent;
		this.extra = extra;
		this.files = files;
	}

}
