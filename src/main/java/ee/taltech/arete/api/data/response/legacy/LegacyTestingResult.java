package ee.taltech.arete.api.data.response.legacy;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LegacyTestingResult {

    String stdout;
    String stderr;
    Double grade_type_code;
    String name;
    Double percentage;
    String test_file;

}
