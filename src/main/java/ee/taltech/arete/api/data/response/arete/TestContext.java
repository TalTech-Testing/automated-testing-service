package ee.taltech.arete.api.data.response.arete;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "text_context")
@Entity
@JsonClassDescription("Test results from test file")
public class TestContext {


	@OneToMany(cascade = {CascadeType.ALL})
	@JsonPropertyDescription("List of unittests tested")
	List<UnitTest> unitTests;
	@Column(columnDefinition = "TEXT")
	@JsonPropertyDescription("Test name")
	String name;
	@Column(columnDefinition = "TEXT")
	@JsonPropertyDescription("Test file path")
	String file;
	@JsonPropertyDescription("Test start time")
	Long startDate;
	@JsonPropertyDescription("Test end time")
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
	Integer grade;
	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
}
