package ee.taltech.arete.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import ee.taltech.arete.api.data.response.arete.File;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;

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
	private String testingPlatform;

	private String returnUrl;

	private String gitStudentRepo;
	//  or
	@Transient
	private File[] source;
	//  and
	private String gitTestSource;

	private String hash;

	private String uniid;

	private String project;

	@Column(length = 16383)
	private HashSet<String> slugs;

	@JsonIgnore
	@Column(columnDefinition = "TEXT")
	private String result;

	private HashSet<String> dockerExtra;
	private HashSet<String> systemExtra;
	private Integer dockerTimeout;

	private Long timestamp;

	private Integer priority;
	private Integer thread;

	public Submission() {
	}

	public Submission(long id, String testingPlatform, String returnUrl, String gitStudentRepo, File[] source, String gitTestSource, String hash, String uniid, String project, HashSet<String> slugs, String result, HashSet<String> dockerExtra,
	                  HashSet<String> systemExtra, Integer dockerTimeout, Long timestamp, Integer priority, Integer thread) {
		this.testingPlatform = testingPlatform;
		this.returnUrl = returnUrl;
		this.gitStudentRepo = gitStudentRepo;
		this.source = source;
		this.gitTestSource = gitTestSource;
		this.hash = hash;
		this.uniid = uniid;
		this.project = project;
		this.slugs = slugs;
		this.result = result;
		this.dockerExtra = dockerExtra;
		this.systemExtra = systemExtra;
		this.dockerTimeout = dockerTimeout;
		this.timestamp = timestamp;
		this.priority = priority;
		this.thread = thread;
	}
}
