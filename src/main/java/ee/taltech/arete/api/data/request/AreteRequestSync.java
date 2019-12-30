package ee.taltech.arete.api.data.request;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.sun.istack.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AreteRequestSync {

	@NotNull
	@JsonPropertyDescription("URL or ssh for test repository.")
	private String gitTestSource;

	@NotNull
	@JsonPropertyDescription("Image used for testing. Currently available: [java, python]")
	private String testingPlatform;

	@NotNull
	@JsonPropertyDescription("List of student source files")
	private List<SourceFile> source;

	@JsonPropertyDescription("Default is second from the end in url. https://gitlab.cs.ttu.ee/iti0102-2019/ex.git > project = iti0102-2019. Specify project, if its not second from end")
	private String project;
	@JsonPropertyDescription("No defaults. You can add (stylecheck)")
	private HashSet<String> dockerExtra;
	@JsonPropertyDescription("No defaults. You can add (noMail, noTesterFiles, noStd, noFeedback)")
	private HashSet<String> systemExtra;
	@JsonPropertyDescription("Default docker timeout is 120 seconds")
	private Integer dockerTimeout;
	@JsonPropertyDescription("Default priority is 5")
	private Integer priority;
	// For integration tests. You can use them.. but use async while you are at it.
	private String returnUrl;
	private String hash;

	@JsonPropertyDescription("Security measurement")
	private String token;

	@Getter
	@Builder
	@ToString
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
