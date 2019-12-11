package ee.taltech.arete.api.data.response.hodor_studenttester;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StyleError {
	String fileName;
	String severityLevel;
	Integer lineNo;
	Integer columnNo;
	String message;
}
