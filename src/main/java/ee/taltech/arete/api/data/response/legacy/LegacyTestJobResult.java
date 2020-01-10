package ee.taltech.arete.api.data.response.legacy;

import ee.taltech.arete.api.data.response.arete.File;
import ee.taltech.arete.api.data.response.hodor_studenttester.TestingResult;
import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LegacyTestJobResult {

    private Double percent;
    private List<LegacyTestingResult> results;
    private List<File> files;
    private String extra;
    private String output;

}
