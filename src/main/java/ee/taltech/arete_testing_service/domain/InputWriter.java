package ee.taltech.arete_testing_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class InputWriter {

	public String contentRoot = "/student";

	public String extra;

	public String testRoot = "/tester";
}
