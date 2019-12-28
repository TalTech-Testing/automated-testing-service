package ee.taltech.arete.api.data.response.arete;

import ee.taltech.arete.api.data.response.hodor_studenttester.*;
import ee.taltech.arete.domain.Submission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "response")
@Entity
public class AreteResponse {

	@OneToMany(cascade = {CascadeType.ALL})
	List<Error> errors = new ArrayList<>();
	@OneToMany(cascade = {CascadeType.ALL})
	List<File> files = new ArrayList<>();
	@OneToMany(cascade = {CascadeType.ALL})
	List<File> testFiles = new ArrayList<>();
	@OneToMany(cascade = {CascadeType.ALL})
	List<TestContext> testSuites = new ArrayList<>();
	@OneToMany(cascade = {CascadeType.ALL})
	List<ConsoleOutput> consoleOutputs = new ArrayList<>();
	@Column(columnDefinition = "TEXT")
	String output;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	Integer totalCount;
	String totalGrade;
	Integer totalPassedCount;
	Integer style = 100;

	public AreteResponse(Submission submission, String message) { //Failed submission
		Error error = new Error.ErrorBuilder().columnNo(0).lineNo(0).fileName("tester").message(message).build();
		this.output = message;
		this.errors.add(error);

		if (!submission.getSystemExtra().contains("noStd")) {
			consoleOutputs.add(new ConsoleOutput.ConsoleOutputBuilder().content(submission.getResult()).build());
		}

		if (submission.getResponse() == null) {
			submission.setResponse(new ArrayList<>());
		}
		submission.getResponse().add(this);
	}

	public AreteResponse(Submission submission, hodorStudentTesterResponse response) { //Successful submission

		for (TestingResult result : response.getResults()) {

			if (result.getTotalCount() != null) {
				totalCount = result.getTotalCount();
			}

			if (result.getTotalGrade() != null) {
				totalGrade = result.getTotalGrade();
			}

			if (result.getTotalPassedCount() != null) {
				totalPassedCount = result.getTotalPassedCount();
			}

			if (result.getErrors() != null) {
				for (StyleError warning : result.getErrors()) {
					Error areteWarning = new Error.ErrorBuilder()
							.columnNo(warning.getColumnNo())
							.lineNo(warning.getLineNo())
							.fileName(warning.getFileName())
							.message(warning.getMessage())
							.kind("style error")
							.build();
					errors.add(areteWarning);
					style = 0;
				}
			}

			if (result.getFiles() != null) {
				for (HodorFile file : result.getFiles()) {
					File areteFile = new File.FileBuilder()
							.path(file.getPath())
							.contents(file.getContents())
							.build();
					if (file.getIsTest()) {
						if (!submission.getSystemExtra().contains("noTesterFiles")) {
							testFiles.add(areteFile);
						}
					} else {
						files.add(areteFile);
					}
				}
			}

			if (result.getDiagnosticList() != null) {
				for (Diagnostic warning : result.getDiagnosticList()) {
					Error areteWarning = new Error.ErrorBuilder()
							.columnNo(warning.getColumnNo())
							.lineNo(warning.getLineNo())
							.fileName(warning.getFile())
							.message(warning.getMessage())
							.hint(warning.getHint())
							.kind(warning.getKind())
							.kind("style error")
							.build();
					errors.add(areteWarning);
				}
			}

			if (result.getTestContexts() != null) {
				testSuites.addAll(result.getTestContexts());
			}

		}

		for (TestingResult result : response.getResults()) {
			if (result.getOutput() != null) {
				output = constructOutput(submission);
			}
		}

		if (!submission.getSystemExtra().contains("noStd")) {
			consoleOutputs.add(new ConsoleOutput.ConsoleOutputBuilder().content(submission.getResult()).build());
		}

		if (submission.getResponse() == null) {
			submission.setResponse(new ArrayList<>());
		}
		submission.getResponse().add(this);
	}

	private static void tr(StringBuilder output) {
		output.append("<tr style='border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
	}

	private static void td(StringBuilder output) {
		output.append("<td style='color:#D5DDE5;background:#4E5066;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
	}

	private static void td(StringBuilder output, String extra) {
		output.append("<td style='color:#D5DDE5;background:#4E5066;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;' ").append(extra).append(">");
	}

	private static void th(StringBuilder output) {
		output.append("<th style='color:#D5DDE5;background:#1b1e24;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
	}

	private String constructOutput(Submission submission) {
		StringBuilder output = new StringBuilder();

		output.append(String.format("<h2>Testing results for %s</h2>", submission.getUniid()));
		output.append(String.format("<p>Submission hash: %s</p>", submission.getHash()));

		long totalSize = 0;
		long totalPassed = 0;

		long totalWeight = 0;
		long totalPassedWeight = 0;

		errorTable(output);


		for (TestContext context : testSuites) {

			if (context.unitTests.size() != 0) {

				output.append("<br>");
				testsTable(submission, output, context);

				List<String> positive = Arrays.asList("success", "passed");

				long size = context.unitTests.size();
				totalSize += size;
				output.append(String.format("<p>Number of tests: %s</p>", size));

				long passed = context.unitTests.stream().filter(x -> positive.contains(x.status.toLowerCase())).count();
				totalPassed += passed;
				output.append(String.format("<p>Passed tests: %s</p>", passed));

				long weights = context.unitTests.stream()
						.map(x -> x.weight)
						.mapToInt(Integer::intValue)
						.sum();
				totalWeight += weights;
				output.append(String.format("<p>Total weight: %s</p>", weights));

				long passedWeights = context.unitTests.stream()
						.filter(x -> positive.contains(x.status.toLowerCase()))
						.map(x -> x.weight)
						.mapToInt(Integer::intValue)
						.sum();
				totalPassedWeight += passedWeights;
				output.append(String.format("<p>Passed weight: %s</p>", passedWeights));
				output.append(String.format("<p>Percentage: %s%s</p>", Math.round((float) passedWeights / (float) weights * 100 * 100.0) / 100.0, "%"));
			}
		}

		this.totalCount = Math.toIntExact(totalSize);
		this.totalPassedCount = Math.toIntExact(totalPassed);

		output.append("<br>");
		output.append("<br>");
		output.append("<h2>Overall</h2>");

		output.append(String.format("<p>Total number of tests: %s</p>", totalSize));
		output.append(String.format("<p>Total passed tests: %s</p>", totalPassed));
		output.append(String.format("<p>Total weight: %s</p>", totalWeight));
		output.append(String.format("<p>Total Passed weight: %s</p>", totalPassedWeight));

		output.append(String.format("<p>Total Percentage: %s%s</p>", Math.round((float) totalPassedWeight / (float) totalWeight * 100 * 100.0) / 100.0, "%"));

		output.append("<br>");
		output.append("<br>");
		output.append(String.format("<p>Timestamp: %s</p>", submission.getTimestamp()));
		return output.toString();
	}

	private void testsTable(Submission submission, StringBuilder output, TestContext context) {
		output.append("<br>");
		output.append("<table style='width:100%;border: 1px solid black;border-collapse: collapse;'>");

		TestsHeader(output);

		context.unitTests.sort((o1, o2) -> {
			if (o1.status.equals(o2.status)) {
				return 0;
			}

			List<String> results = Arrays.asList("success", "partial_success", "passed", "skipped", "not_run", "failure", "failed", "not_set", "unknown");

			int place1 = results.indexOf(o1.status.toLowerCase());
			int place2 = results.indexOf(o2.status.toLowerCase());
			return place1 < place2 ? -1 : 1;
		});

		for (UnitTest test : context.unitTests) {

			tr(output);
			td(output);
			output.append(test.name);

			if (!submission.getSystemExtra().contains("noFeedback") && test.getPrintExceptionMessage() != null && test.getPrintExceptionMessage() && test.getExceptionMessage() != null) {

				List<String> lines;
				try {
					lines = Files.readAllLines(Paths.get("src/main/java/ee/taltech/arete/api/data/response/arete/emojis.txt"));
//					System.out.println(lines);
				} catch (IOException ignored) {
					lines = new ArrayList<>();
					lines.add("idk lol");
				}
				Random rand = new Random();
				String randomElement = lines.get(rand.nextInt(lines.size()));

				output.append(
						String.format("<br><a style='color:red;'>%s</a>: %s", test.getExceptionClass().equals("") ? "UnresolvedException" : test.getExceptionClass(),
								test.getExceptionMessage().equals("") ? randomElement : test.getExceptionMessage())
				);

			}

			if (!submission.getSystemExtra().contains("noFeedback") && test.getPrintStackTrace() != null && test.getPrintStackTrace() && test.getStackTrace() != null) {

				output.append(String.format("<br>%s:%s", "Stacktrace", test.getStackTrace()));

			}

			output.append("</td>");

			td(output);

			List<String> GREEN = Arrays.asList("success", "passed");
			List<String> YELLOW = Arrays.asList("partial_success", "skipped");
			List<String> RED = Arrays.asList("not_run", "failure", "failed", "not_set", "unknown");

			if (GREEN.contains(test.status.toLowerCase())) {
				output.append(String.format("<p style='color:greenyellow;'>%s</p>", test.status));
			} else if (YELLOW.contains(test.status.toLowerCase())) {
				output.append(String.format("<p style='color:yellow;'>%s</p>", test.status));
			} else if (RED.contains(test.status.toLowerCase())) {
				output.append(String.format("<p style='color:red;'>%s</p>", test.status));
			}

			output.append("</td>");

			td(output);
			output.append(test.timeElapsed);
			output.append("</td>");

			td(output);
			output.append(test.weight);
			output.append("</td>");

			output.append("</tr>");

		}
		output.append("</table>");
	}

	private void TestsHeader(StringBuilder output) {
		th(output);
		output.append("Test");
		output.append("</th>");

		th(output);
		output.append("Result");
		output.append("</th>");

		th(output);
		output.append("Time (ms)");
		output.append("</th>");

		th(output);
		output.append("Weight");
		output.append("</th>");
	}

	private void errorTable(StringBuilder output) {
		output.append("<br>");
		output.append("<table style='width:100%;border: 1px solid black;border-collapse: collapse;' id='errors'>");

		tr(output);

		th(output);
		output.append("File");
		output.append("</th>");

		th(output);
		output.append("Line");
		output.append("</th>");

		th(output);
		output.append("Column");
		output.append("</th>");

		th(output);
		output.append("Error");
		output.append("</th>");

		output.append("</tr>");

		for (Error error : errors) {

			tr(output);
			String[] name;
			if (error.fileName != null) {
				name = error.fileName.split("/");
			} else {
				name = new String[]{"null"};
			}

			td(output);
			output.append(name[name.length - 1]);
			output.append("</td>");

			td(output);
			output.append(error.lineNo);
			output.append("</td>");

			td(output);
			output.append(error.columnNo);
			output.append("</td>");

			td(output);
			output.append(error.message);
			output.append("</td>");


			output.append("</tr>");

			if (error.hint != null && !error.hint.equals("")) {
				tr(output);

				td(output);
				output.append("Hint");
				output.append("</td>");

				td(output, "colspan='3'");
				output.append(error.hint.replace("\n", ""));
				output.append("</td>");
				output.append("</tr>");
			}
		}

		output.append("</table>");

		output.append("<br>");
		output.append(String.format("Style percentage: %s%s", style, "%"));
		output.append("<br>");
	}
}
