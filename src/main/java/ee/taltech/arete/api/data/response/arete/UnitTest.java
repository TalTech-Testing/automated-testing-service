package ee.taltech.arete.api.data.response.arete;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "unit_test")
@Entity
public class UnitTest {

	@ElementCollection(targetClass = String.class)
	List<String> groupsDependedUpon;

	String status;
	Integer weight;
	Boolean printExceptionMessage;
	Boolean printStackTrace;
	Long timeElapsed;
	@ElementCollection(targetClass = String.class)
	List<String> methodsDependedUpon;
	@Column(columnDefinition = "TEXT")
	String stackTrace;
	String name;
	@OneToMany(cascade = {CascadeType.ALL})
	List<TesterStd> stdout;
	String exceptionClass;
	String exceptionMessage;
	@OneToMany(cascade = {CascadeType.ALL})
	List<TesterStd> stderr;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

}
