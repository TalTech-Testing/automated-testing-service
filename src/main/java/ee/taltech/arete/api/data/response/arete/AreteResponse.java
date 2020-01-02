package ee.taltech.arete.api.data.response.arete;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import ee.taltech.arete.api.data.response.hodor_studenttester.*;
import ee.taltech.arete.domain.Submission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
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
@JsonClassDescription("Response sent to Moodle")
public class AreteResponse {

	@Column(length = 1023)
	String version = "arete_2.0";

	@JsonPropertyDescription("List of style, compilation and other errors")
	@OneToMany(cascade = {CascadeType.ALL})
	List<Error> errors = new ArrayList<>();

	@JsonPropertyDescription("List of student files")
	@OneToMany(cascade = {CascadeType.ALL})
	List<File> files = new ArrayList<>();

	@JsonPropertyDescription("List of test files")
	@OneToMany(cascade = {CascadeType.ALL})
	List<File> testFiles = new ArrayList<>();

	@JsonPropertyDescription("List of test suites which each contains unit-tests. Each test file produces an test suite")
	@OneToMany(cascade = {CascadeType.ALL})
	List<TestContext> testSuites = new ArrayList<>();

	@JsonPropertyDescription("Console outputs from docker")
	@OneToMany(cascade = {CascadeType.ALL})
	List<ConsoleOutput> consoleOutputs = new ArrayList<>();

	@JsonPropertyDescription("HTML result for student")
	@Column(columnDefinition = "TEXT")
	String output;

	@JsonPropertyDescription("Number of tests")
	Integer totalCount = 0;

	@Column(length = 1023)
	@JsonPropertyDescription("Passed percentage")
	String totalGrade = "0";

	@JsonPropertyDescription("Number of passed tests")
	Integer totalPassedCount = 0;

	@JsonPropertyDescription("Style percentage")
	Integer style = 100;

	@Column(length = 1023)
	@JsonPropertyDescription("Slug ran for student. for example pr01_something")
	String slug;

	@Column(length = 1023)
	@JsonPropertyDescription("Security Token")
	String token;

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	public AreteResponse(String slug, Submission submission, String message) { //Failed submission
		Error error = new Error.ErrorBuilder().columnNo(0).lineNo(0).fileName("tester").message(message).build();
		this.output = message;
		this.errors.add(error);

		if (submission.getSystemExtra() != null && !submission.getSystemExtra().contains("noStd")) {
			consoleOutputs.add(new ConsoleOutput.ConsoleOutputBuilder().content(submission.getResult()).build());
		}

		if (submission.getResponse() == null) {
			submission.setResponse(new ArrayList<>());
		}

		submission.getResponse().add(this);
		this.token = submission.getToken();
		this.slug = slug;
	}

	public AreteResponse(String slug, Submission submission, hodorStudentTesterResponse response) { //Successful submission

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
					style = 0;
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
		this.token = submission.getToken();
		this.slug = slug;
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

				String totalGrade = String.format("%s", Math.round((float) passedWeights / (float) weights * 100 * 100.0) / 100.0);
				output.append(String.format("<p>Percentage: %s%s</p>", totalGrade, "%"));
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

		String totalGrade = String.format("%s", Math.round((float) totalPassedWeight / (float) totalWeight * 100 * 100.0) / 100.0);
		this.totalGrade = totalGrade;
		output.append(String.format("<p>Total Percentage: %s%s</p>", totalGrade, "%"));

		output.append("<br>");
		output.append("<br>");
		output.append(String.format("<p>Timestamp: %s</p>", submission.getTimestamp()));
		return output.toString();
	}

	private static String getRandomElement() {
		String[] lines = new String[]{"ʘ‿ʘ", "ಠ_ಠ", "(╯°□°）╯︵ ┻━┻", "┬─┬﻿ ノ( ゜-゜ノ)", "┬─┬⃰͡ (ᵔᵕᵔ͜ )", "┻━┻ ︵ヽ(`Д´)ﾉ︵﻿ ┻━┻", "ლ(｀ー´ლ)", "ʕ•ᴥ•ʔ", "ʕᵔᴥᵔʔ", "ʕ •`ᴥ•´ʔ", "(｡◕‿◕｡)", "（ ﾟДﾟ）", "¯\\_(ツ)_/¯", "¯\\(°_o)/¯", "(`･ω･´)", "(╬ ಠ益ಠ)", "ლ(ಠ益ಠლ)", "☜(⌒▽⌒)☞", "ε=ε=ε=┌(;*´Д`)ﾉ", "ヽ(´▽`)/", "ヽ(´ー｀)ノ", "ᵒᴥᵒ#", "V•ᴥ•V", "ฅ^•ﻌ•^ฅ", "（ ^_^）o自自o（^_^ ）", "ಠ‿ಠ", "( ͡° ͜ʖ ͡°)", "ಥ_ಥ", "ಥ﹏ಥ", "٩◔̯◔۶", "ᕙ(⇀‸↼‶)ᕗ", " ᕦ(ò_óˇ)ᕤ", "⊂(◉‿◉)つ", "q(❂‿❂)p", "⊙﹏⊙", "¯\\_(⊙︿⊙)_/¯", "°‿‿°", "¿ⓧ_ⓧﮌ", "(⊙.☉)7", "(´･_･`)", "щ（ﾟДﾟщ）", "٩(๏_๏)۶", "ఠ_ఠ", "ᕕ( ᐛ )ᕗ", "(⊙_◎)", "ミ●﹏☉ミ", "༼∵༽ ༼⍨༽ ༼⍢༽ ༼⍤༽", "ヽ༼ ಠ益ಠ ༽ﾉ", "t(-_-t)", "(ಥ⌣ಥ)", "(づ￣ ³￣)づ", "(づ｡◕‿‿◕｡)づ", "(ノಠ ∩ಠ)ノ彡( \\o°o)\\", "｡ﾟ( ﾟஇ‸இﾟ)ﾟ｡", "༼ ༎ຶ ෴ ༎ຶ༽", "“ヽ(´▽｀)ノ”", "┌(ㆆ㉨ㆆ)ʃ", "눈_눈", "( ఠൠఠ )ﾉ", "乁( ◔ ౪◔)「      ┑(￣Д ￣)┍", "(๑•́ ₃ •̀๑)", "⁽⁽ଘ( ˊᵕˋ )ଓ⁾⁾", "◔_◔", "♥‿♥", "ԅ(≖‿≖ԅ)", "( ˘ ³˘)♥", "( ˇ෴ˇ )", "ヾ(-_- )ゞ", "♪♪ ヽ(ˇ∀ˇ )ゞ", "ヾ(´〇`)ﾉ♪♪♪", "ʕ •́؈•̀)", "ლ(•́•́ლ)", "(ง'̀-'́)ง", "◖ᵔᴥᵔ◗ ♪ ♫", "{•̃_•̃}", "(ᵔᴥᵔ)", "(Ծ‸ Ծ)", "(•̀ᴗ•́)و ̑̑", "[¬º-°]¬", "(☞ﾟヮﾟ)☞", "(っ•́｡•́)♪♬", "(҂◡_◡)", "ƪ(ړײ)‎ƪ​​", "⥀.⥀", "ح˚௰˚づ", "♨_♨", "(._.)", "(⊃｡•́‿•̀｡)⊃", "(∩｀-´)⊃━☆ﾟ.*･｡ﾟ", "(っ˘ڡ˘ς)", "( ఠ ͟ʖ ఠ)", "( ͡ಠ ʖ̯ ͡ಠ)", "( ಠ ʖ̯ ಠ)", "(งツ)ว", "(◠﹏◠)", "(ᵟຶ︵ ᵟຶ)", "(っ▀¯▀)つ", "ʚ(•｀", "(´ж｀ς)", "(° ͜ʖ͡°)╭∩╮", "ʕʘ̅͜ʘ̅ʔ", "ح(•̀ж•́)ง †", "-`ღ´-", "(⩾﹏⩽)", "ヽ( •_)ᕗ", "~(^-^)~", "\\(ᵔᵕᵔ)/", "ᴖ̮ ̮ᴖ", "ಠಠ", "{ಠʖಠ}'", "idk lol"};
		Random rand = new Random();
		return lines[rand.nextInt(lines.length)];
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

				String randomElement = getRandomElement();

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

}
