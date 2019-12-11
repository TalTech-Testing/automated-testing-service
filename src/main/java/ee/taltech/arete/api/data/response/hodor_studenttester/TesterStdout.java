package ee.taltech.arete.api.data.response.hodor_studenttester;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TesterStdout {

	String thread;
	Boolean truncated;
	String content;
}
