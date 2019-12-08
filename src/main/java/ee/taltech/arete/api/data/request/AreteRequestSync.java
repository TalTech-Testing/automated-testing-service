package ee.taltech.arete.api.data.request;

import com.sun.istack.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AreteRequestSync {

	@NotNull
	private String gitTestSource; // URL for tests

	@NotNull
	private String testingPlatform; // Image used for testing. Currently available: ["java", "python"]

	@NotNull
	private List<SourceFile> source;
	private String project; // Default is second from the end in url. https://gitlab.cs.ttu.ee/iti0102-2019/ex > project = iti0102-2019. Specify project, if its not second from end
	private String[] dockerExtra; // Default is "stylecheck".
	private String[] systemExtra; // No defaults. You can add "noMail"
	private Integer dockerTimeout; // Default docker timeout is 120 seconds
	private Integer priority; // Default priority is 5
	// For integration tests. You can use them.. but use async while you are at it.
	private String returnUrl;
	private String hash;

	@Getter
	@Builder
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SourceFile {

		@NotNull
		private String path; // EX01IdCode/src/ee/taltech/iti0202/idcode/IDCodeTest.java for example

		@NotNull
		private String contents; // contents of the file.

	}

}
