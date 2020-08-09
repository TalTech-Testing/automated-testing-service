package ee.taltech.arete.api.data.response.arete;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file")
@Entity
@JsonClassDescription("File class")
public class File {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@JsonPropertyDescription("Path for the file")
	@Column(length = 1023)
	private String path;

	@JsonPropertyDescription("File content")
	@Column(columnDefinition = "TEXT")
	private String contents;

}
