package ee.taltech.arete.api.data.response.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ee.taltech.arete.api.data.SourceFile;
import ee.taltech.arete.api.data.response.TestingResult;
import ee.taltech.arete.api.data.response.legacy.LegacyTestingResult;
import ee.taltech.arete.api.data.response.legacy.LegacyTestingResultDetails;
import ee.taltech.arete.api.data.response.modern.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class TestingResultDeserializer extends JsonDeserializer<TestingResult> {

	@Override
	public TestingResult deserialize(JsonParser jsonParser,
	                                 DeserializationContext deserializationContext) throws IOException {
		JsonNode treeNode = jsonParser.getCodec().readTree(jsonParser);
		String type = treeNode.get("type").asText();
		switch (type) {
			case "hodor_legacy":
				return deserializeLegacyFeedback(treeNode, type);

			case "hodor_studenttester":
				return deserializeModernFeedback(treeNode, type);

			default:
				throw new UnsupportedOperationException("Type '" + type + "' is not supported!");
		}
	}

	private ModernTestingResult deserializeModernFeedback(JsonNode treeNode, String type) {
		return ModernTestingResult.builder()
				.type(type)
				.version(treeNode.get("version").asText())
//                .stdout(treeNode.get("stdout").asText())
//                .stderr(treeNode.get("stderr").asText())
//                .output(treeNode.get("output").asText())
				.contentRoot(treeNode.get("contentRoot").asText())
				.testRoot(treeNode.get("testRoot").asText())
				.results(deserializeModernResultDetails(treeNode.get("results").elements()))
				.build();
	}

	private LegacyTestingResult deserializeLegacyFeedback(JsonNode treeNode, String type) {
		return LegacyTestingResult.builder()
				.type(type)
				.version(treeNode.get("version").asText())
				.stdout(treeNode.get("stdout").asText())
				.stderr(treeNode.get("stderr").asText())
				.percent(treeNode.get("percent").asInt())
				.output(treeNode.get("output").asText())
				.extra(treeNode.get("extra").asText())
				.files(deserializeFiles(treeNode.get("files").elements()))
				.results(deserializeLegacyResultDetails(treeNode.get("results").elements()))
				.build();
	}

	private List<LegacyTestingResultDetails> deserializeLegacyResultDetails(Iterator<JsonNode> jsonNodes) {
		List<LegacyTestingResultDetails> testingResultDetails = new LinkedList<>();
		while (jsonNodes.hasNext()) {
			JsonNode nextJsonNode = jsonNodes.next();
			testingResultDetails.add(
					LegacyTestingResultDetails.builder()
							.percentage(nextJsonNode.get("percentage").asInt())
							.percent(nextJsonNode.get("percent").asInt())
							.output(nextJsonNode.get("output").asText())
							.stdout(nextJsonNode.get("stdout").asText())
							.stderr(nextJsonNode.get("stderr").asText())
							.gradeTypeCode(nextJsonNode.get("grade_type_code").asText())
							.build());
		}
		return testingResultDetails;
	}

	private List<ModernTestingResultDetails> deserializeModernResultDetails(Iterator<JsonNode> jsonNodes) {
		List<ModernTestingResultDetails> testingResultDetails = new LinkedList<>();
		while (jsonNodes.hasNext()) {
			JsonNode nextJsonNode = jsonNodes.next();
			JsonNode filesNode = nextJsonNode.get("files");
			testingResultDetails.add(
					ModernTestingResultDetails.builder()
							.code(nextJsonNode.get("code").asInt())
							.identifier(nextJsonNode.get("identifier").asText())
							.result(nextJsonNode.get("result").asText())
							.files(filesNode != null ? deserializeFiles(filesNode.elements()) : null)
							.output(nullOrText(nextJsonNode.get("output")))
							.totalCount(nullOrInteger(nextJsonNode.get("totalCount")))
							.totalPassedCount(nullOrInteger(nextJsonNode.get("totalPassedCount")))
							.totalGrade(parseTotalGrade(nextJsonNode.get("totalGrade")))
							.securityViolation(nullOrBoolean(nextJsonNode.get("securityViolation")))
							.testContexts(deserializeTextContexts(nextJsonNode.get("testContexts")))
							.diagnosticsList(deserializeDiagnostics(nextJsonNode.get("diagnosticList")))
							.build());
		}
		return testingResultDetails;
	}

	private List<Diagnostic> deserializeDiagnostics(JsonNode jsonNode) {
		if (jsonNode == null) {
			return null;
		}
		List<Diagnostic> diagnostics = new LinkedList<>();
		Iterator<JsonNode> elements = jsonNode.elements();
		while (elements.hasNext()) {
			JsonNode nextJsonNode = elements.next();
			diagnostics.add(
					Diagnostic.builder()
							.kind(nextJsonNode.get("kind").asText())
							.lineNo(nextJsonNode.get("lineNo").asInt())
							.columnNo(nextJsonNode.get("columnNo").asInt())
							.message(nextJsonNode.get("message").asText())
							.code(nextJsonNode.get("code").asText())
							.file(nextJsonNode.get("file").asText())
							.hint(nextJsonNode.get("hint").asText())
							.affected(nextJsonNode.get("affected").asText())
							.sensitive(nextJsonNode.get("sensitive").asBoolean())
							.build());
		}
		return diagnostics;
	}

	private List<TestContext> deserializeTextContexts(JsonNode jsonNode) {
		if (jsonNode == null) {
			return null;
		}
		List<TestContext> testContexts = new LinkedList<>();
		Iterator<JsonNode> elements = jsonNode.elements();
		while (elements.hasNext()) {
			JsonNode nextJsonNode = elements.next();
			testContexts.add(
					TestContext.builder()
							.name(nextJsonNode.get("name").asText())
							.file(nextJsonNode.get("file").asText())
							.startDate(dateTimeFromTimestamp(nextJsonNode.get("startDate").asLong()))
							.endDate(dateTimeFromTimestamp(nextJsonNode.get("endDate").asLong()))
							.mode(nextJsonNode.get("mode").asText())
							.welcomeMessage(nextJsonNode.get("welcomeMessage").asText())
							.identifier(nextJsonNode.get("identifier").asInt())
							.count(nextJsonNode.get("count").asInt())
							.passedCount(nextJsonNode.get("passedCount").asInt())
							.weight(nextJsonNode.get("weight").asInt())
							.grade(nextJsonNode.get("grade").asDouble())
							.unitTests(deserializeUnitTests(nextJsonNode.get("unitTests").elements()))
							.build());
		}
		return testContexts;
	}

	private List<UnitTest> deserializeUnitTests(Iterator<JsonNode> elements) {
		List<UnitTest> unitTests = new LinkedList<>();
		while (elements.hasNext()) {
			JsonNode nextJsonNode = elements.next();
			unitTests.add(
					UnitTest.builder()
							.status(UnitTest.UnitTestStatus.valueOf(nextJsonNode.get("status").asText()))
							.weight(nextJsonNode.get("weight").asInt())
							.description(nextJsonNode.get("description").asText())
							.printExceptionMessage(nextJsonNode.get("printExceptionMessage").asBoolean())
							.printStackTrace(nextJsonNode.get("printStackTrace").asBoolean())
							.timeElapsed(nextJsonNode.get("timeElapsed").asInt())
							.name(nextJsonNode.get("name").asText())
							.stackTrace(nullOrText(nextJsonNode.get("stackTrace")))
							.exceptionClass(nullOrText(nextJsonNode.get("exceptionClass")))
							.exceptionMessage(nullOrText(nextJsonNode.get("exceptionMessage")))
							.stderr(deserializeStreamOutput(nextJsonNode.get("stderr")))
							.stdout(deserializeStreamOutput(nextJsonNode.get("stdout")))
							.build());
		}
		return unitTests;
	}

	private List<StreamOutput> deserializeStreamOutput(JsonNode jsonNode) {
		if (jsonNode == null) {
			return null;
		}
		Iterator<JsonNode> elements = jsonNode.elements();
		List<StreamOutput> streamOutputs = new LinkedList<>();
		while (elements.hasNext()) {
			JsonNode nextJsonNode = elements.next();
			streamOutputs.add(
					StreamOutput.builder()
							.thread(nextJsonNode.get("thread").asText())
							.content(nextJsonNode.get("content").asText())
							.truncated(nextJsonNode.get("truncated").asBoolean())
							.build());
		}
		return streamOutputs;
	}

	private LocalDateTime dateTimeFromTimestamp(long timestamp) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId());
	}

	private Integer parseTotalGrade(JsonNode totalGrade) {
		return totalGrade == null || totalGrade.isNull() || totalGrade.asText().equals("NaN") ? null :
				totalGrade.asInt();
	}

	private String nullOrText(JsonNode node) {
		return node == null || node.isNull() ? null : node.asText();
	}

	private Integer nullOrInteger(JsonNode node) {
		return node == null || node.isNull() ? null : node.asInt();
	}

	private Boolean nullOrBoolean(JsonNode node) {
		return node == null || node.isNull() ? null : node.asBoolean();
	}

	private List<SourceFile> deserializeFiles(Iterator<JsonNode> jsonNodes) {
		List<SourceFile> sourceFiles = new LinkedList<>();
		while (jsonNodes.hasNext()) {
			JsonNode nextJsonNode = jsonNodes.next();
			JsonNode isTestField = nextJsonNode.get("isTest");
			sourceFiles.add(
					SourceFile.builder()
							.path(nextJsonNode.get("path").asText())
							.contents(nextJsonNode.get("contents").asText())
//                            .test(isTestField.isNull() ? null : isTestField.asBoolean()) // TODO
							.build());
		}
		return sourceFiles;
	}

}
