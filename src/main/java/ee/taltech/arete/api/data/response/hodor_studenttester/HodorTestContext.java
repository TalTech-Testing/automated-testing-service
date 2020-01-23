package ee.taltech.arete.api.data.response.hodor_studenttester;

import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HodorTestContext {

    List<HodorUnitTest> unitTests;
    String name;
    String file;
    Long startDate;
    Long endDate;
    String mode;
    String welcomeMessage;
    Integer identifier;
    Integer count;
    Integer weight;
    Integer passedCount;
    Double grade;
}
