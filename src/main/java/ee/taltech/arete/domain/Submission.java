package ee.taltech.arete.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import ee.taltech.arete.api.data.response.arete.File;
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

    @NotNull
    private String testingPlatform;

    private String returnUrl;

    private String gitStudentRepo;
    //  or
    private List<File> source;

    private String gitTestSource;
    // or
    private List<File> testSource;

    private String hash;

    private String uniid; // gitlab namespace: envomp

	private String email;

    private String course; // gitlab namespace with path for tester: iti0102-2019/ex

    private String folder; // gitlab path for student: iti0102-2019

    private Set<String> slugs;

	private Set<String> initialSlugs;

    private String commitMessage;

    @JsonIgnore
    private String result;

    private Set<String> dockerExtra = new HashSet<>();

    private Set<String> systemExtra = new HashSet<>();

	private Set<String> groupingFolders = new HashSet<>();

    private Integer dockerTimeout;

    private Long timestamp;

	private Long recievedTimeStamp;

    private Integer priority;

    private Integer thread;

    private JsonNode returnExtra; // private stuff here

    private String waitingroom;

}
