package ee.taltech.arete_testing_service.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import ee.taltech.arete.java.response.arete.FileDTO;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Submission {

	private String commitMessage;

	private String course; // gitlab namespace with path for tester: iti0102-2019/ex

	private String dockerContentRoot;

	private String dockerExtra;

	private String dockerTestRoot;

	private Integer dockerTimeout;

	private String email;

	private String folder; // gitlab path for student: iti0102-2019

	private String gitStudentRepo;

	private String gitTestRepo;

	@Builder.Default
	private Set<String> groupingFolders = new HashSet<>();

	private String hash;

	private Set<String> initialSlugs;

	private Integer priority;

	private Long receivedTimestamp;

	@JsonIgnore
	private String result;

	private JsonNode returnExtra; // private stuff here

	private String returnUrl;

	private Set<String> slugs;

	private List<FileDTO> source;

	@Builder.Default
	private Set<String> systemExtra = new HashSet<>();

	private List<FileDTO> testSource;

	@NotNull
	private String testingPlatform;

	private Integer thread;

	private Long timestamp;

	private String uniid; // gitlab namespace: envomp

	private String waitingroom;
}
