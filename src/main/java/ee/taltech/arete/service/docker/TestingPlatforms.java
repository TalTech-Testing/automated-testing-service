package ee.taltech.arete.service.docker;

import ee.taltech.arete.domain.Submission;

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

	public static void correctTesterInput(Submission submission) {
		String[] output = new String[submission.getExtra().length];
		int i = 0;
		for (String elem : submission.getExtra()) {
			if (submission.getProject().equals("java") && elem.equals("stylecheck")) {
				output[i] = "-r CHECKSTYLE";
			} else {
				output[i] = elem;
			}
			i++;
		}
		submission.setExtra(output);
	}

	public final String language;
	public final String image;

	TestingPlatforms(String languge, String image) {
		this.language = languge;
		this.image = image;
	}
}
