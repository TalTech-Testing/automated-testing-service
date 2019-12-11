package ee.taltech.arete.api.data.response.hodor_studenttester;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Diagnostic {

	String kind;
	Integer lineNo;
	Integer columnNo;
	String message;
	String code;
	String file;
	String hint;
	String affected;
	Boolean sensitive;
}
