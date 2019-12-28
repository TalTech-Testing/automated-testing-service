package ee.taltech.arete.api.data.response.arete;

import lombok.*;

import javax.persistence.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file")
@Entity
public class File {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String path;
	@Column(columnDefinition = "TEXT")
	private String contents;

}
