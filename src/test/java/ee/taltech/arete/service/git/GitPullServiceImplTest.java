package ee.taltech.arete.service.git;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static ee.taltech.arete.initializers.SubmissionInitializer.getControllerEndpointSubmission;

@RunWith(SpringRunner.class)
@SpringBootTest
class GitPullServiceImplTest {

	@Autowired
	private GitPullService gitPullService;

	@Test
	void pullJob() {
		try {
			File f = new File("students/envomp");
			FileUtils.cleanDirectory(f); //clean out directory (this is optional -- but good know)
			FileUtils.forceDelete(f); //delete directory
		} catch (Exception ignored) {
		}
		gitPullService.repositoryMaintenance(getControllerEndpointSubmission());
		gitPullService.repositoryMaintenance(getControllerEndpointSubmission());
	}

}