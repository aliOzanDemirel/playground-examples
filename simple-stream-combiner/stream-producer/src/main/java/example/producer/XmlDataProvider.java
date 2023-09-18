package example.producer;

import java.util.stream.Stream;

public interface XmlDataProvider {

    Stream<String> streamXmlData();
}