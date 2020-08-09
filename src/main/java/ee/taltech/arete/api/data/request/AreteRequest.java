package ee.taltech.arete.api.data.request;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.istack.NotNull;
import lombok.*;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonClassDescription("Async request from Moodle")
public class AreteRequest {

    @JsonPropertyDescription("Image used for testing. Currently available: [java, python]")
    private String testingPlatform;

    @JsonPropertyDescription("URL where result is sent.")
    private String returnUrl;

    @JsonPropertyDescription("URL or ssh for student repository")
    private String gitStudentRepo;
    // or. One of the options must be chosen
    @JsonPropertyDescription("List of student source files")
    private List<SourceFile> source;

    @JsonPropertyDescription("URL or ssh for test repository")
    private String gitTestSource;
    //or
    @JsonPropertyDescription("List of test source files")
    private List<SourceFile> testSource;

    @JsonPropertyDescription("Slugs to be tested")
    private Set<String> slugs;

    @JsonPropertyDescription("Specify hash to test that specific hash. Otherwise the latest hash of student repository will be tested, if git student repo is present.")
    private String hash;

    @JsonPropertyDescription("If gitStudentRepo is used, default is second from the end in url. https://gitlab.cs.ttu.ee/envomp/iti0102-2019.git > uniid = envomp. Specify uniid, if its not second from end. Otherwise not needed.")
    private String uniid;

	@JsonPropertyDescription("Where student mail is sent. Default is uniid@ttu.ee")
	private String eMail;

    @JsonPropertyDescription("Default is last in url. https://gitlab.cs.ttu.ee/envomp/iti0102-2019.git > project = iti0102-2019. Specify project, if its not in last position.")
    private String project;

    @JsonPropertyDescription("No defaults. You can add (stylecheck) or something. It is sent to smaller tester. Look the possibilities from the small tester repository for more details.")
    private Set<String> dockerExtra;

    @JsonPropertyDescription("No defaults. You can add (anonymous, noOverride, noMail, noFiles, noTesterFiles, noStudentFiles, noStd, noFeedback, minimalFeedback, noFun)")
    private Set<String> systemExtra;

    @JsonPropertyDescription("Default docker timeout is 120 seconds")
    private Integer dockerTimeout;

    @JsonPropertyDescription("Default priority is 5")
    private Integer priority;

	@JsonPropertyDescription("When override testing timestamp")
	private Long timestamp;

    @JsonPropertyDescription("values that are returned the same way they were given in")
    private JsonNode returnExtra;

	@Data
	@Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceFile {

        @NotNull
        @JsonPropertyDescription("EX01IdCode/src/ee/taltech/iti0202/idcode/IDCodeTest.java for example")
        private String path;

        @NotNull
        @JsonPropertyDescription("Contents of the file")
        private String contents;

    }
}
