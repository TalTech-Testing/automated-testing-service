package ee.taltech.arete.api.data.response.hodor_studenttester;

import ee.taltech.arete.api.data.response.arete.ConsoleOutput;
import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HodorUnitTest {

    List<String> groupsDependedUpon;
    String status;
    Integer weight;
    Boolean printExceptionMessage;
    Boolean printStackTrace;
    Long timeElapsed;
    List<String> methodsDependedUpon;
    String stackTrace;
    String name;
    List<ConsoleOutput> stdout;
    String exceptionClass;
    String exceptionMessage;
    List<ConsoleOutput> stderr;

}