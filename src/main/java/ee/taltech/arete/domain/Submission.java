package ee.taltech.arete.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@ToString
@Entity
@Getter
@Setter
@Builder()
@Table(name = "submission")
public class Submission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String uniid;
	private String hash;

	private String[] slugs;
	private String project;
	private String projectBase;

	private String testingPlatform;
	private String returnUrl;

	private String[] extra;

	private Long timestamp;
	private Integer priority;

	public Submission() {
	}

	public Submission(long id, String uniid, String hash, String[] slugs, String project, String projectBase, String testingPlatform, String returnUrl, String[] extra, Long timestamp, Integer priority) {
		this.uniid = uniid;
		this.hash = hash;
		this.slugs = slugs;
		this.project = project;
		this.projectBase = projectBase;
		this.testingPlatform = testingPlatform;
		this.returnUrl = returnUrl;
		this.extra = extra;
		this.timestamp = timestamp;
		this.priority = priority;
	}
}
