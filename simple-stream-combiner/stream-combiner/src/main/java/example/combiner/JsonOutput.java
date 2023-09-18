package example.combiner;

import java.util.ArrayList;
import java.util.List;

public interface JsonOutput {

    String JSON_OUTPUT_FORMAT = "{ \"data\": { \"timestamp\":%d, \"amount\":\"%.6f\" }}";

    default String toJson(XmlData xmlData) {
        return String.format(JSON_OUTPUT_FORMAT, xmlData.timestamp, xmlData.amount);
    }

    void write(XmlData xmlData);

    class StdoutWriter implements JsonOutput {
        @Override
        public void write(XmlData xmlData) {
            String jsonStr = toJson(xmlData);
            System.out.println("JSON OUTPUT STREAM -> " + jsonStr);
        }
    }

    class BufferWriter implements JsonOutput {

        private final List<String> xmlDataJsons = new ArrayList<>();

        @Override
        public void write(XmlData xmlData) {
            String jsonStr = toJson(xmlData);
            xmlDataJsons.add(jsonStr);
        }

        public List<String> getXmlDataJsons() {
            return xmlDataJsons;
        }
    }
}
