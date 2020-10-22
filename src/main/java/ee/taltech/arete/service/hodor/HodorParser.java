package ee.taltech.arete.service.hodor;


import ee.taltech.arete.java.TestStatus;
import ee.taltech.arete.java.response.arete.*;
import ee.taltech.arete.java.response.hodor_studenttester.*;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HodorParser {

    @SneakyThrows
    public static AreteResponseDTO parse(HodorStudentTesterResponse response) {
        AreteResponseDTO areteResponse = AreteResponseDTO.builder().build();

        for (TestingResult result : response.getResults()) {

            if (result.getTotalCount() != null) {
                areteResponse.setTotalCount(result.getTotalCount());
            }

            if (result.getTotalGrade() != null) {
                try {
                    areteResponse.setTotalGrade(Double.valueOf(result.getTotalGrade()));
                } catch (Exception e) {
                    areteResponse.setTotalGrade(-1.0);
                }
            }

            if (result.getTotalPassedCount() != null) {
                areteResponse.setTotalPassedCount(result.getTotalPassedCount());
            }

            if (result.getErrors() != null) {
                for (StyleError warning : result.getErrors()) {
                    ErrorDTO areteWarning = ErrorDTO.builder()
                            .columnNo(warning.getColumnNo())
                            .lineNo(warning.getLineNo())
                            .fileName(warning.getFileName())
                            .message(warning.getMessage())
                            .kind("style error")
                            .build();
                    areteResponse.getErrors().add(areteWarning);
                    areteResponse.setStyle(0);
                }
            }

            if (result.getFiles() != null) {
                for (HodorFile file : result.getFiles()) {
                    FileDTO areteFile = FileDTO.builder()
                            .path(file.getPath())
                            .contents(file.getContents())
                            .build();

                    if (file.getIsTest()) {
                        areteResponse.getTestFiles().add(areteFile);
                    } else {
                        areteResponse.getFiles().add(areteFile);
                    }

                }
            }

            if (result.getDiagnosticList() != null) {
                for (Diagnostic warning : result.getDiagnosticList()) {
                    ErrorDTO areteWarning = ErrorDTO.builder()
                            .columnNo(warning.getColumnNo())
                            .lineNo(warning.getLineNo())
                            .fileName(warning.getFile())
                            .message(warning.getMessage())
                            .hint(warning.getHint())
                            .kind(warning.getKind() == null ? "Diagnostic error" : warning.getKind())
                            .build();
                    areteResponse.getErrors().add(areteWarning);
                    areteResponse.setStyle(0);
                }
            }

            if (result.getTestContexts() != null) {

                List<String> PASSED = Arrays.asList("success", "passed", "ok", "yes");
                List<String> SKIPPED = Arrays.asList("partial_success", "skipped");
                List<String> FAILED = Arrays.asList("not_run", "failure", "failed", "not_set", "unknown", "no");

                for (HodorTestContext context : result.getTestContexts()) {
                    List<UnitTestDTO> unitTests = new ArrayList<>();
                    for (HodorUnitTest test : context.getUnitTests()) {

                        TestStatus status;
                        if (PASSED.contains(test.getStatus().toLowerCase())) {
                            status = TestStatus.PASSED;
                        } else if (FAILED.contains(test.getStatus().toLowerCase())) {
                            status = TestStatus.FAILED;
                        } else {
                            status = TestStatus.SKIPPED;
                        }

                        UnitTestDTO unitTest = UnitTestDTO.builder()
                                .exceptionClass(test.getExceptionClass())
                                .printExceptionMessage(test.getPrintExceptionMessage())
                                .exceptionMessage(test.getExceptionMessage())
                                .groupsDependedUpon(test.getGroupsDependedUpon())
                                .methodsDependedUpon(test.getMethodsDependedUpon())
                                .printStackTrace(test.getPrintStackTrace())
                                .stackTrace(test.getStackTrace())
                                .stdout(test.getStdout())
                                .stderr(test.getStderr())
                                .name(test.getName())
                                .timeElapsed(test.getTimeElapsed())
                                .weight(test.getWeight())
                                .status(status)
                                .build();

                        unitTests.add(unitTest);
                    }

                    TestContextDTO testContext = TestContextDTO.builder()
                            .endDate(context.getEndDate())
                            .file(context.getFile())
                            .grade(context.getGrade())
                            .name(context.getName())
                            .passedCount(context.getPassedCount())
                            .startDate(context.getStartDate())
                            .endDate(context.getEndDate())
                            .weight(context.getWeight())
                            .unitTests(unitTests)
                            .build();

                    areteResponse.getTestSuites().add(testContext);

                }
            }
        }

        return areteResponse;
    }
}
