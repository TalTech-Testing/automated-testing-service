package ee.taltech.arete_testing_service.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import ee.taltech.arete.java.TestingEnvironment;
import ee.taltech.arete.java.UvaConfiguration;
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

    @NotNull
    private String testingPlatform;

    private String returnUrl;

    private String gitStudentRepo;
    //  or
    private List<FileDTO> source;

    private String gitTestRepo;
    // or
    private List<FileDTO> testSource;

    private String hash;

    @NotNull
    private String uniid; // gitlab namespace: envomp

    private String email;

    private String course; // gitlab namespace with path for tester: iti0102-2019/ex

    private String folder; // gitlab path for student: iti0102-2019

    private Set<String> slugs;

    private Set<String> initialSlugs;

    private String commitMessage;

    @JsonIgnore
    private String result;

    @Builder.Default
    private Set<String> dockerExtra = new HashSet<>();

    @Builder.Default
    private Set<String> systemExtra = new HashSet<>();

    @Builder.Default
    private Set<String> groupingFolders = new HashSet<>();

    private Integer dockerTimeout;

    private Long timestamp;

    private Long receivedTimestamp;

    private Integer priority;

    private Integer thread;

    private JsonNode returnExtra; // private stuff here

    private String waitingroom;

    @Builder.Default
    private TestingEnvironment testingEnvironment = TestingEnvironment.DOCKER;

    private UvaConfiguration uvaConfiguration; // when using TestingEnvironment.UVA

}
