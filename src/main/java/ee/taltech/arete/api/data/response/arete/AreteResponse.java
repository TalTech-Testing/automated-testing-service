package ee.taltech.arete.api.data.response.arete;

import ee.taltech.arete.api.data.response.hodor_studenttester.*;
import ee.taltech.arete.domain.Submission;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AreteResponse {

	ArrayList<Error> errors = new ArrayList<>();
	ArrayList<File> files = new ArrayList<>();
	ArrayList<File> testFiles = new ArrayList<>();
	ArrayList<TestContext> testSuites = new ArrayList<>();
	ArrayList<ConsoleOutput> consoleOutputs = new ArrayList<>();
	String output;
	Integer totalCount;
	String totalGrade;
	Integer totalPassedCount;
	Integer style = 100;

	public AreteResponse(Submission submission, String message) { //Failed submission
		Error error = new Error.ErrorBuilder().columnNo(0).lineNo(0).fileName("tester").message(message).build();
		output = message;
		this.errors.add(error);

		if (!submission.getSystemExtra().contains("noStd")) {
			consoleOutputs.add(new ConsoleOutput.ConsoleOutputBuilder().content(submission.getResult()).build());
		}
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
				output = constructOutput(result, submission);
			}
		}

		if (!submission.getSystemExtra().contains("noStd")) {
			consoleOutputs.add(new ConsoleOutput.ConsoleOutputBuilder().content(submission.getResult()).build());
		}
	}

	private String constructOutput(TestingResult result, Submission submission) {
		StringBuilder output = new StringBuilder();

		output.append("<table style=\"width:100%\">");
		long totalSize = 0;
		long totalPassed = 0;

		long totalWeight = 0;
		long totalPassedWeight = 0;


		output.append("<tr>");
		output.append("<th>");
		output.append(String.format("Errors: "));
		output.append("</th>");
		output.append("</tr>");

		for (Error error : errors) {

			output.append("<tr>");
			output.append("<th>");
			String[] name;
			if (error.fileName != null) {
				name = error.fileName.split("/");
			} else {
				name = new String[]{"null"};
			}
			output.append(String.format("file: %s - line: %s - column: %s </th></tr><tr><th> %s",
					name[name.length - 1], error.lineNo, error.columnNo, error.message.replace("\n", "</th></tr><tr><th>")));
			output.append("</th>");
			output.append("</tr>");

			if (error.hint != null && !error.hint.equals("")) {
				output.append("<tr>");
				output.append("<th>");
				output.append(String.format("hint: %s", error.hint.replace("\n", "")));
				output.append("</th>");
				output.append("</tr>");
			}

			output.append("<tr><th></th></tr>");
			output.append("<tr><th></th></tr>");
		}

		output.append("<tr><th></th></tr>");
		output.append("<tr><th></th></tr>");

		output.append("<tr>");
		output.append("<th>");
		output.append(String.format("Style percentage: %s%s", style, "%"));
		output.append("</th>");
		output.append("</tr>");

		output.append("<tr><th></th></tr>");
		output.append("<tr><th></th></tr>");
		output.append("<tr><th></th></tr>");
		output.append("<tr><th></th></tr>");


		for (TestContext context : testSuites) {

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
				output.append("<tr>");
				output.append("<th>");
				output.append(String.format("%s: %s (%s ms) weight %s", test.name, test.status, test.timeElapsed, test.weight));
				output.append("</th>");
				output.append("</tr>");
				if (!submission.getSystemExtra().contains("noFeedback") && test.getPrintExceptionMessage() && test.getExceptionMessage() != null) {

					output.append("<tr>");
					output.append("<th>");
					output.append(String.format("%s: %s", test.getExceptionClass(), test.getExceptionMessage()));
					output.append("</th>");
					output.append("</tr>");
				}

				if (!submission.getSystemExtra().contains("noFeedback") && test.getPrintStackTrace() && test.getStackTrace() != null) {

					output.append("<tr>");
					output.append("<th>");
					output.append(String.format("Stacktrace: %s", test.getStackTrace().replace("\n", "</th></tr><tr><th>")));
					output.append("</th>");
					output.append("</tr>");
				}
			}

			output.append("<tr><th></th></tr>");
			output.append("<tr><th></th></tr>");

			List<String> positive = Arrays.asList("success", "passed");

			output.append("<tr>");
			output.append("<th>");
			long size = context.unitTests.size();
			totalSize += size;
			output.append(String.format("Number of tests: %s", size));
			output.append("</th>");
			output.append("</tr>");

			output.append("<tr>");
			output.append("<th>");
			long passed = context.unitTests.stream().filter(x -> positive.contains(x.status.toLowerCase())).count();
			totalPassed += passed;
			output.append(String.format("Passed tests: %s", passed));
			output.append("</th>");
			output.append("</tr>");

			output.append("<tr>");
			output.append("<th>");
			long weights = context.unitTests.stream()
					.map(x -> x.weight)
					.mapToInt(Integer::intValue)
					.sum();
			totalWeight += weights;
			output.append(String.format("Total weight: %s", weights));
			output.append("</th>");
			output.append("</tr>");

			output.append("<tr>");
			output.append("<th>");
			long passedWeights = context.unitTests.stream()
					.filter(x -> positive.contains(x.status.toLowerCase()))
					.map(x -> x.weight)
					.mapToInt(Integer::intValue)
					.sum();
			totalPassedWeight += passedWeights;
			output.append(String.format("Passed weight: %s", passedWeights));
			output.append("</th>");
			output.append("</tr>");

			output.append("<tr>");
			output.append("<th>");
			output.append(String.format("Percentage: %s%s", Math.round((float) passedWeights / (float) weights * 100 * 100.0) / 100.0, "%"));
			output.append("</th>");
			output.append("</tr>");

			output.append("<tr><th></th></tr>");
			output.append("<tr><th></th></tr>");
			output.append("<tr><th></th></tr>");
			output.append("<tr><th></th></tr>");

		}

		this.totalCount = Math.toIntExact(totalSize);
		this.totalPassedCount = Math.toIntExact(totalPassed);

		output.append("<tr>");
		output.append("<th>");
		output.append(String.format("Overall"));
		output.append("</th>");
		output.append("</tr>");

		output.append("<tr><th></th></tr>");
		output.append("<tr><th></th></tr>");

		output.append("<tr>");
		output.append("<th>");
		output.append(String.format("Total number of tests: %s", totalSize));
		output.append("</th>");
		output.append("</tr>");

		output.append("<tr>");
		output.append("<th>");
		output.append(String.format("Total passed tests: %s", totalPassed));
		output.append("</th>");
		output.append("</tr>");

		output.append("<tr>");
		output.append("<th>");
		output.append(String.format("Total weight: %s", totalWeight));
		output.append("</th>");
		output.append("</tr>");

		output.append("<tr>");
		output.append("<th>");
		output.append(String.format("Total Passed weight: %s", totalPassedWeight));
		output.append("</th>");
		output.append("</tr>");

		output.append("<tr>");
		output.append("<th>");
		output.append(String.format("Total Percentage: %s%s", Math.round((float) totalPassedWeight / (float) totalWeight * 100 * 100.0) / 100.0, "%"));
		output.append("</th>");
		output.append("</tr>");

		output.append("</table>");
		return output.toString();
	}
}
