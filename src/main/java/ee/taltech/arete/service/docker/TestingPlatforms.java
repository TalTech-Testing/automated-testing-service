package ee.taltech.arete.service.docker;

import java.util.HashMap;
import java.util.Map;

public enum TestingPlatforms {
	JAVA("java", "automatedtestingservice/java-tester"),
	PYTHON("python", "automatedtestingservice/python-tester"),
	PROLOG("prolog", "automatedtestingservice/prolog-tester"),
	FSHARP("f#", "");

	public static final Map<String, TestingPlatforms> BY_LABEL = new HashMap<>();

	static {
		for (TestingPlatforms e : values()) {
			BY_LABEL.put(e.language, e);
		}
	}

	public final String language;
	public final String image;

	TestingPlatforms(String languge, String image) {
		this.language = languge;
		this.image = image;
	}
}
