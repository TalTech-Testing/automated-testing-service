package ee.taltech.arete.domain;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.sun.istack.NotNull;
import lombok.*;

@ToString
@Getter
@Setter
@Builder()
@AllArgsConstructor
@NoArgsConstructor
public class TestUpdate {
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
