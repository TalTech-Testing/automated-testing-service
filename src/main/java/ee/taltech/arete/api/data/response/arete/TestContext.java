package ee.taltech.arete.api.data.response.arete;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonClassDescription("Test results from test file")
public class TestContext {

	@JsonPropertyDescription("List of unittests tested")
	List<UnitTest> unitTests;

	@JsonPropertyDescription("Test name")
	String name;

	@JsonPropertyDescription("Test file path")
	String file;

	@JsonPropertyDescription("Test start time in milliseconds")
	Long startDate;

	@JsonPropertyDescription("Test end time in milliseconds")
	Long endDate;

//	String mode;
//	String welcomeMessage;
//	Integer identifier;
//	Integer count;

	@JsonPropertyDescription("Sum of test weights")
	Integer weight;

	@JsonPropertyDescription("Number of passed tests")
	Integer passedCount;

	@JsonPropertyDescription("Total grade for this test file")
	Double grade;

}
