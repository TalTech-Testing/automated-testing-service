package ee.taltech.arete.service.docker;

import java.util.HashMap;
import java.util.Map;

public enum TestingPlatforms {
	JAVA("java", "Dockerfile-java-test-job"),
	PYTHON("python", "testers/tester-python-old/Dockerfile"),
	PROLOG("prolog", "testers/tester-prolog/Dockerfile"),
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
