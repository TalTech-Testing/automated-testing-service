package ee.taltech.arete.api.data.request;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.sun.istack.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AreteTestUpdate {

    @NotNull
    @JsonPropertyDescription("Git hook repository")
    private Repository repository;

    @JsonPropertyDescription("Folder where tests are saved")
    private String course;

}
