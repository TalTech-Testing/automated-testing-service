package ee.taltech.arete.api.data.response.arete;

import ee.taltech.arete.api.data.response.hodor_studenttester.*;
import ee.taltech.arete.domain.Submission;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AreteResponse {

	ArrayList<Error> errors = new ArrayList<>();
	ArrayList<File> files = new ArrayList<>();
	ArrayList<File> testFiles = new ArrayList<>();
	ArrayList<TestContext> tests = new ArrayList<>();
	ArrayList<ConsoleOutput> consoleOutputs = new ArrayList<>();
	String output;

	public AreteResponse(Submission submission, String message) { //Failed submission
		Error error = new Error.ErrorBuilder().columnNo(0).lineNo(0).fileName("tester").message(message).build();
		output = message;
		this.errors.add(error);

		if (!Arrays.asList(submission.getSystemExtra()).contains("noStd")) {
			consoleOutputs.add(new ConsoleOutput.ConsoleOutputBuilder().content(submission.getResult()).build());
		}
	}

	public AreteResponse(Submission submission, hodorStudentTesterResponse response) { //Successful submission
		for (TestingResult result : response.getResults()) {

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
				}
			}

			if (result.getFiles() != null) {
				for (HodorFile file : result.getFiles()) {
					File areteFile = new File.FileBuilder()
							.path(file.getPath())
							.contents(file.getContents())
							.build();
					if (file.getIsTest()) {
						if (!Arrays.asList(submission.getSystemExtra()).contains("noTesterFiles")) {
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
				tests.addAll(result.getTestContexts());
			}

			if (result.getOutput() != null) {
				output = result.getOutput();
			}

		}

		if (!Arrays.asList(submission.getSystemExtra()).contains("noStd")) {
			consoleOutputs.add(new ConsoleOutput.ConsoleOutputBuilder().content(submission.getResult()).build());
		}
	}
}
