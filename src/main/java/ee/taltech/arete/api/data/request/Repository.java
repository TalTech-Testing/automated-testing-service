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
public class Repository {

	@NotNull
	@JsonPropertyDescription("URL or ssh for test repository.")
	private String url;

	@JsonPropertyDescription("Default is second from the end in url. https://gitlab.cs.ttu.ee/iti0102-2019/ex.git > course = iti0102-2019. Specify course, if its not second from end")
	private String name;

}
