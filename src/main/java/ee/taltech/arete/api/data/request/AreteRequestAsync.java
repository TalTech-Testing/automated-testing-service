package ee.taltech.arete.api.data.request;

import com.sun.istack.NotNull;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AreteRequestAsync {

	@NotNull
	private String testingPlatform; // Image used for testing. Currently available: ["java", "python"]

	@NotNull
	private String returnUrl; // URL where result is sent.

	@NotNull
	private String gitStudentRepo; // URL for student repository


	private String hash; // Specify hash to test that specific hash. Otherwise the latest hash of student repository will be tested.
	private String uniid; // Default is second from the end in url. https://gitlab.cs.ttu.ee/envomp/iti0102-2019.git > uniid = envomp. Specify uniid, if its not second from end
	private String project; // Default is last in url. https://gitlab.cs.ttu.ee/envomp/iti0102-2019.git > project = iti0102-2019. Specify project, if its not in last position.

	private String[] dockerExtra; // Default is "stylecheck".
	private String[] systemExtra; // No defaults. You can add "noMail", "noTesterFiles", "noStd"
	private Integer dockerTimeout; // Default docker timeout is 120 seconds
	private Integer priority; // Default priority is 5

}
