package ee.taltech.arete.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
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

	@NotNull
	private String uniid;
	@NotNull
	private String project;
	@NotNull
	private String projectBase;
	@NotNull
	private String testingPlatform;
	@NotNull
	private String returnUrl;

	private String hash;
	private String[] slugs;

	@JsonIgnore
	@Transient
	private final StringBuilder result = new StringBuilder();
	private String[] dockerExtra;
	private String[] systemExtra;
	private Long timestamp;
	private Integer priority;
	private Integer thread;
	private Integer dockerTimeout;

	public Submission() {
	}

	public Submission(long id, String uniid, String project, String projectBase, String testingPlatform, String returnUrl, String hash, String[] slugs, String[] dockerExtra, String[] systemExtra, Long timestamp, Integer priority,
	                  Integer thread, Integer dockerTimeout) {
		this.uniid = uniid;
		this.project = project;
		this.projectBase = projectBase;
		this.testingPlatform = testingPlatform;
		this.returnUrl = returnUrl;
		this.hash = hash;
		this.slugs = slugs;
		this.dockerExtra = dockerExtra;
		this.systemExtra = systemExtra;
		this.timestamp = timestamp;
		this.priority = priority;
		this.thread = thread;
		this.dockerTimeout = dockerTimeout;
	}
}
