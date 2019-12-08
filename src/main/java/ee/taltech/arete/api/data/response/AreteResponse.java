package ee.taltech.arete.api.data.response;

import lombok.*;

import java.util.Set;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AreteResponse {

	private Set<TestingResult> resultSet;
	private String version;
	private int responseStatus;
	private String gitCommit;

}
