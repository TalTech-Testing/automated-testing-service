package ee.taltech.arete.api.data.response.arete;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonClassDescription("Occured style, compilation and other errors")
public class Error {

	@JsonPropertyDescription("Error message")
	String message;

	@JsonPropertyDescription("Error kind(styleError, compilationError, other)")
	String kind;

	@JsonPropertyDescription("File, where error occured")
	String fileName;

	@JsonPropertyDescription("Line, where error occured")
	Integer lineNo;

	@JsonPropertyDescription("Column, where error occured")
	Integer columnNo;

	@JsonPropertyDescription("Hint, to fix the error")
	String hint;

}
