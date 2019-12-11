package ee.taltech.arete.api.data.response.arete;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Error {
	String kind;
	String fileName;
	Integer lineNo;
	Integer columnNo;
	String message;
	String hint;
}
