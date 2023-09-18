package example.producer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class UtilFunctionTest {

    @Test
    void testFileReader_classpathFile() {

        String fileInClasspath = "test_stream.txt";
        List<String> result = UtilFunction.linesFromFile(fileInClasspath);
        Assertions.assertEquals(4, result.size(), "unexpected amount of lines read from file");
    }

    @Test
    void testFileReader_systemFile() throws IOException {

        Path tempFile = Files.createTempFile("temp_stream_1", ".txt");
        Files.writeString(tempFile,
                "<data> <timestamp>1</timestamp> <amount>1.1</amount> </data>\n" +
                        "<data> <timestamp>2</timestamp> <amount>2.2</amount> </data>");

        String filePath = tempFile.toAbsolutePath().toString();
        List<String> result = UtilFunction.linesFromFile(filePath);
        Assertions.assertEquals(2, result.size(), "unexpected amount of lines read from file");
    }
}
