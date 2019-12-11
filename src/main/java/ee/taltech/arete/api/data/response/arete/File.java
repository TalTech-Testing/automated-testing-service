package ee.taltech.arete.api.data.response.arete;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class File {

	private String path;
	private String contents;

}
