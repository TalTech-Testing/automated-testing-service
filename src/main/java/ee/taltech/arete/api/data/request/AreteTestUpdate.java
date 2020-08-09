package ee.taltech.arete.api.data.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.sun.istack.NotNull;
import lombok.*;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AreteTestUpdate {

	@NotNull
	@JsonPropertyDescription("Git hook project")
	private Project project;

	@NotNull
	@JsonPropertyDescription("Git hook project")
	private List<Commit> commits;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Project {

		@NotNull
		@JsonPropertyDescription("URL or ssh for test repository.")
		private String url;

		@JsonPropertyDescription("https://gitlab.cs.ttu.ee/iti0102-2019/ex.git > namespace = iti0102-2019")
		private String namespace;

		@JsonPropertyDescription("https://gitlab.cs.ttu.ee/iti0102-2019/ex.git > path_with_namespace = iti0102-2019/ex")
		private String path_with_namespace;

	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Commit {

		@NotNull
		@JsonPropertyDescription("Author of the commit")
		private Author author;

		@JsonPropertyDescription("Added files")
		private Set<String> added;

		@JsonPropertyDescription("Modified files")
		private Set<String> modified;

		@JsonPropertyDescription("Removed files")
		private Set<String> removed;

	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Author {

		@NotNull
		@JsonPropertyDescription("Name of the author")
		private String name;

		@JsonPropertyDescription("email of the author")
		private String email;

	}
}
