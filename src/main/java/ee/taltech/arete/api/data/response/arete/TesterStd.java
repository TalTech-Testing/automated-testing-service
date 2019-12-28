package ee.taltech.arete.api.data.response.arete;

import lombok.*;

import javax.persistence.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tester_std")
@Entity
public class TesterStd {

	String thread;
	Boolean truncated;
	@Column(columnDefinition = "TEXT")
	String content;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
}
