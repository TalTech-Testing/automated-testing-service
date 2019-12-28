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

	@Transient
	private HashSet<String> slugs;

	@Transient
	private String result;

	private HashSet<String> dockerExtra;
	private HashSet<String> systemExtra;
	private Integer dockerTimeout;

	private Long timestamp;

	private Integer priority;
	private Integer thread;

	@OneToMany(cascade = {CascadeType.ALL})
	@JsonIgnore
	private List<AreteResponse> response = new ArrayList<>();

}
