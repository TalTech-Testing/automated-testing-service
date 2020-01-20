//package ee.taltech.arete.service.docker;
//
//import ee.taltech.arete.domain.Submission;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//
//public enum TestingPlatforms {
//	JAVA("java", "automatedtestingservice/java-tester"),
//	PYTHON("python", "automatedtestingservice/python-tester"),
//	PROLOG("prolog", "automatedtestingservice/prolog-tester"),
//	FSHARP("f#", "");
//
//	public static final Map<String, TestingPlatforms> BY_LABEL = new HashMap<>();
//
//	static {
//		for (TestingPlatforms e : values()) {
//			BY_LABEL.put(e.language, e);
//		}
//	}
//
//	public final String language;
//	public final String image;
//
//	TestingPlatforms(String languge, String image) {
//		this.language = languge;
//		this.image = image;
//	}
//
//	public static void correctTesterInput(Submission submission) {
//		HashSet<String> output = new HashSet<>();
//		int i = 0;
//		for (String elem : submission.getDockerExtra()) {
//			if (submission.getTestingPlatform().equals("java") && elem.equals("stylecheck")) {
//				output.add("-r FILEWRITER,COMPILER,TESTNG,REPORT,CHECKSTYLE");
//			} else {
//				output.add(elem);
//			}
//			i++;
//		}
//		submission.setDockerExtra(output);
//	}
//}
