package ee.taltech.arete.api.data.response.modern;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Diagnostic {

	private String kind;
	private int lineNo;
	private int columnNo;
	private String message;
	private String code;
	private String file;
	private String hint;
	private String affected;
	private boolean sensitive;

}
