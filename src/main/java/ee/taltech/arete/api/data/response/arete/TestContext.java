package ee.taltech.arete.api.data.response.arete;

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
public class TestContext {

	@OneToMany(cascade = {CascadeType.ALL})
	List<UnitTest> unitTests;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	String name;
	String file;
	Long startDate;
	Long endDate;
	//	String mode;
//	String welcomeMessage;
//	Integer identifier;
//	Integer count;
	Integer weight;
	Integer passedCount;
	Integer grade;
}
