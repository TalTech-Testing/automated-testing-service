package ee.taltech.arete_testing_service;

import ee.taltech.arete_testing_service.service.FileBuffer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Utils {
    public static List<String> tailFile(final Path source, final int noOfLines) throws IOException {
        try (Stream<String> stream = Files.lines(source)) {
            FileBuffer fileBuffer = new FileBuffer(noOfLines);
            stream.forEach(fileBuffer::collect);
            return fileBuffer.getLines();
        }
    }
}
