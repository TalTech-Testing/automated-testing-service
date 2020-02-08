package ee.taltech.arete.api.data.response.arete;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor
@JsonClassDescription("Stderr or Stdout")
public class ConsoleOutput {

	@JsonPropertyDescription("Std message")
	String content;

	public ConsoleOutput(String content) {
		this.content = content;
	}
}