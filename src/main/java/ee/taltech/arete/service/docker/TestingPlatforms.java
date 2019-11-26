package ee.taltech.arete.service.docker;

import java.util.HashMap;
import java.util.Map;

public enum TestingPlatforms {
	JAVA("java", "Dockerfile-java-test-job"),
	PYTHON("python", "Dockerfile-prolog-test-job"),
	PROLOG("prolog", "Dockerfile-python-test-job"),
	FSHARP("f#", "");

	public static final Map<String, TestingPlatforms> BY_LABEL = new HashMap<>();

	static {
		for (TestingPlatforms e : values()) {
			BY_LABEL.put(e.language, e);
		}
	}

	public final String dockerfileLocation;
	public final String language;

	TestingPlatforms(String languge, String dockerfileLocation) {
		this.language = languge;
		this.dockerfileLocation = dockerfileLocation;
	}
}
