package ee.taltech.arete.api.data.response.arete;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import javax.persistence.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "error")
@Entity
@JsonClassDescription("Occured style, compilation and other errors")
public class Error {

	@JsonPropertyDescription("Error message")
	@Column(columnDefinition = "TEXT")
	String message;

	@JsonPropertyDescription("Error kind(styleError, compilationError, other)")
	String kind;
	@JsonPropertyDescription("File, where error occured")
	@Column(columnDefinition = "TEXT")
	String fileName;
	@JsonPropertyDescription("Line, where error occured")
	Integer lineNo;
	@JsonPropertyDescription("Column, where error occured")
	Integer columnNo;
	@JsonPropertyDescription("Hint, to fix the error")
	@Column(columnDefinition = "TEXT")
	String hint;

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
}
