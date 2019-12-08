package ee.taltech.arete.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SourceFile {

	private String path;
	private String contents;

	@JsonIgnore
	private Boolean test;

}
