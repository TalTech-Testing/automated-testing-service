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
    @JsonPropertyDescription("Git hook project")
    private Project project;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {

        @NotNull
        @JsonPropertyDescription("URL or ssh for test repository.")
        private String url;

    }
}
