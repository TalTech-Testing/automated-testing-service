package ee.taltech.arete.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.istack.NotNull;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.api.data.response.arete.File;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ToString
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "submission")
public class Submission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull
	@Column(length = 1023)
	private String testingPlatform;

	@Column(length = 1023)
	private String returnUrl;

	@Column(length = 1023)
	private String gitStudentRepo;
	//  or
	@OneToMany(cascade = {CascadeType.ALL})
	private List<File> source;

	@Column(length = 1023)
	private String gitTestSource;
	// or
	@OneToMany(cascade = {CascadeType.ALL})
	private List<File> testSource;

	@Column(length = 1023)
	private String hash;

	@Column(length = 1023)
	private String uniid; // gitlab namespace: envomp

	@Column(length = 1023)
	private String course; // gitlab namespace with path for tester: iti0102-2019/ex

	@Column(length = 1023)
	private String folder; // gitlab path for student: iti0102-2019

	@ElementCollection
	@CollectionTable(name = "slugs", joinColumns = @JoinColumn(name = "id"))
	@Column(length = 1023)
	private Set<String> slugs;

	@Column(length = 1023)
	private String commitMessage;

	@Transient
	@JsonIgnore
	private String result;

	@ElementCollection
	@CollectionTable(name = "docker_extra", joinColumns = @JoinColumn(name = "id"))
	@Column(length = 1023)
	private Set<String> dockerExtra = new HashSet<>();

	@ElementCollection
	@CollectionTable(name = "system_extra", joinColumns = @JoinColumn(name = "id"))
	@Column(length = 1023)
	private Set<String> systemExtra = new HashSet<>();

	private Integer dockerTimeout;

	private Long timestamp;

	private Integer priority;

	@Transient
	private Integer thread;

	@Transient
	private JsonNode returnExtra; // private stuff here

	@Transient
	private String waitingroom;

	@OneToMany(cascade = {CascadeType.ALL})
	@JsonIgnore
	private List<AreteResponse> response = new ArrayList<>();

}
