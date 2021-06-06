package ee.taltech.arete_testing_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OverrideParametersCollection {

    boolean[] changed;
    private Optional<OverrideParameters> testRoot;
    private Optional<OverrideParameters> testGroup;
    private Optional<OverrideParameters> testSlug;
    private Optional<OverrideParameters> studentRoot;
    private Optional<OverrideParameters> studentGroup;
    private Optional<OverrideParameters> studentSlug;

}
