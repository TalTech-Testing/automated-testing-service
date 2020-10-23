package ee.taltech.arete_testing_service.service.arete;

public class TableGenerator {
	public static void tr(StringBuilder output) {
		output.append("<tr style='border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
	}

	public static void td(StringBuilder output) {
		output.append("<td style='color:#D5DDE5;background:#393939;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
	}

	public static void td(StringBuilder output, boolean bright) {
		String hex;

		if (bright) {
			hex = "8FBC8F";
		} else {
			hex = "393939";
		}

		output.append("<td style='color:#D5DDE5;background:#").append(hex).append(";border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
	}

	public static void td_hex(StringBuilder output, String hex) {
		output.append("<td style='color:#D5DDE5;background:#").append(hex).append(";border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
	}

	public static void td_extra(StringBuilder output, String extra) {
		output.append("<td style='color:#D5DDE5;background:#393939;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;' ").append(extra).append(">");
	}

	public static void TestsHeader(StringBuilder output, String headerName) {
		th(output);
		output.append(headerName);
		output.append("</th>");

		th(output);
		output.append("Result");
		output.append("</th>");

		th(output);
		output.append("Time (ms)");
		output.append("</th>");

		th(output);
		output.append("Weight");
		output.append("</th>");
	}

	public static void th(StringBuilder output) {
		output.append("<th style='color:#D5DDE5;background:#1b1e24;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
	}

}
