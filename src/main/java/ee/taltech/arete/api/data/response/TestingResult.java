package ee.taltech.arete.api.data.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ee.taltech.arete.api.data.response.json.TestingResultDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@JsonDeserialize(using = TestingResultDeserializer.class)
@NoArgsConstructor
@AllArgsConstructor
public abstract class TestingResult<DetailsType extends TestingResultDetails> {

	private String type;
	private String version;
	private String stdout;
	private String stderr;
	private String output;
	private List<DetailsType> results;

}
