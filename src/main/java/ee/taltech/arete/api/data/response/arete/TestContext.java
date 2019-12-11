package ee.taltech.arete.api.data.response.arete;

import lombok.*;

import java.util.ArrayList;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TestContext {

	ArrayList<UnitTest> unitTests;
	String name;
	String file;
	Long startDate;
	Long endDate;
	String mode;
	String welcomeMessage;
	Integer identifier;
	Integer count;
	Integer weight;
	Integer passedCount;
	Integer grade;
}
