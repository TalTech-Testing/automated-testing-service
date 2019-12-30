package ee.taltech.arete.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.api.data.response.arete.File;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@ToString
@Entity
@Getter
@Setter
@Builder()
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
	@Transient
	private File[] source;
	//  and
	@Column(length = 1023)
	private String gitTestSource;

	@Column(length = 1023)
	private String hash;

	@Column(length = 1023)
	private String uniid;

	@Column(length = 1023)
	private String course;

	@Column(length = 1023)
	private String folder;

	@Transient
	private HashSet<String> slugs;

	@Transient
	private String result;

	@Column(length = 1023)
	private HashSet<String> dockerExtra;

	@Column(length = 1023)
	private HashSet<String> systemExtra;

	private Integer dockerTimeout;

	private Long timestamp;

	private Integer priority;

	private Integer thread;

	@Column(length = 1023)
	private String token;

	@OneToMany(cascade = {CascadeType.ALL})
	@JsonIgnore
	private List<AreteResponse> response = new ArrayList<>();

}
