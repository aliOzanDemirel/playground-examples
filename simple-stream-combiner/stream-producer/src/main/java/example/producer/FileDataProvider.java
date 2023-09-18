package example.producer;

import java.util.stream.Stream;

public class FileDataProvider implements XmlDataProvider {

    private final String filePath;

    public FileDataProvider(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Stream<String> streamXmlData() {

        // intentionally receiving file as fully loaded and creating new stream over it
        // alternative should be okay too, then file input stream should always be closed by caller
        return UtilFunction.linesFromFile(filePath)
                .stream()
                .peek(it -> UtilFunction.simulateWait(0.5));
    }
}
