package ee.taltech.arete.api.data.response.modern;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TestContext {

	private List<UnitTest> unitTests;
	private String name;
	private String file;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private String mode;
	private String welcomeMessage;
	private int identifier;
	private int count;
	private int passedCount;
	private double grade;
	private int weight;

}
