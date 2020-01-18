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

    @JsonPropertyDescription("Folder where tests are saved")
    private String course;

    @NotNull
    @JsonPropertyDescription("URL or ssh for test repository.")
    private String url;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {

        @NotNull
        @JsonPropertyDescription("URL or ssh for test repository.")
        private String url;

        @JsonPropertyDescription("Default is first after gitlab.cs.ttu.ee. https://gitlab.cs.ttu.ee/iti0102-2019/ex.git > namespace = iti0102-2019. Specify course, if its not first after gitlab.cs.ttu.ee")
        private String namespace;

    }
}
