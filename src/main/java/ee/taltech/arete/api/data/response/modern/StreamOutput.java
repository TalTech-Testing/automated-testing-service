package ee.taltech.arete.api.data.response.modern;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StreamOutput {

	private String thread;
	private boolean truncated;
	private String content;

}
