package ee.taltech.arete.api.data.response.legacy;

import ee.taltech.arete.api.data.response.TestingResultDetails;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LegacyTestingResultDetails extends TestingResultDetails {

	private int percentage;
	private String output;
	private String stderr;
	private String stdout;
	private int percent;
	private String gradeTypeCode;

}
