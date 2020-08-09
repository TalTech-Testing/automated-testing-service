package ee.taltech.arete.api.data.response.arete;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonClassDescription("Stderr or Stdout")
public class ConsoleOutput {

	@JsonPropertyDescription("Std message")
	String content;

}