package ee.taltech.arete_testing_service.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FileBuffer {
	private final int noOfLines;

	private final String[] lines;

	private int offset = 0;

	public FileBuffer(int noOfLines) {
		this.noOfLines = noOfLines;
		this.lines = new String[noOfLines];
	}

	public void collect(String line) {
		lines[offset++ % noOfLines] = line;
	}

	public List<String> getLines() {
		return IntStream.range(offset < noOfLines ? 0 : offset - noOfLines, offset)
				.mapToObj(idx -> lines[idx % noOfLines]).collect(Collectors.toList());
	}
}
