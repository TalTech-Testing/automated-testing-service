package ee.taltech.arete_testing_service.domain;

public class InputWriter {

	public String contentRoot = "/student";
	public String testRoot = "/tester";
	public String extra;

	public InputWriter(String extra) {
		this.extra = extra;
	}
}
