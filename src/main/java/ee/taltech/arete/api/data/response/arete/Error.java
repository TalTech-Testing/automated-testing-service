package ee.taltech.arete.api.data.response.arete;

import lombok.*;

import javax.persistence.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "error")
@Entity
public class Error {

	@Column(columnDefinition = "TEXT")
	String message;

	String kind;
	String fileName;
	Integer lineNo;
	Integer columnNo;
	@Column(columnDefinition = "TEXT")
	String hint;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
}
