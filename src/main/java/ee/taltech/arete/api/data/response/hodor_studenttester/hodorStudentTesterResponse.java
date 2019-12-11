package ee.taltech.arete.api.data.response.hodor_studenttester;

import lombok.*;

import java.util.ArrayList;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class hodorStudentTesterResponse {

	private String version;
	private String type;
	private String contentRoot;
	private String testRoot;
	private ArrayList<TestingResult> results;
	private String extra; // ?

}
