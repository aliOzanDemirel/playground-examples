package example.combiner.deprecated;

import example.combiner.JsonOutput;
import example.combiner.XmlData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class StreamMerger2Test {

    private StreamMerger2 merger;
    private JsonOutput.BufferWriter writer;

    @BeforeEach
    void beforeEach() {
        writer = new JsonOutput.BufferWriter();
        merger = new StreamMerger2(writer, 2);
    }

    @Test
    public void testMerge_1() {

        // A -> 3!
        // B -> -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 -
        // B -> - 2!
        merger.add("B", new XmlData(2, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 - 5!
        // B -> - 2 -
        merger.add("A", new XmlData(5, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 - 5 -
        // B -> - 2 - 3!
        merger.add("B", new XmlData(3, BigDecimal.TEN));
        Assertions.assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        Assertions.assertEquals("{ \"data\": { \"timestamp\":2, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> 3 - 5 - -
        // B -> - - - 3 3!
        merger.add("B", new XmlData(3, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 - 5 - - -
        // B -> - - - 3 3 4!
        merger.add("B", new XmlData(4, BigDecimal.ONE));
        Assertions.assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        Assertions.assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"12.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> - - 5 - - - 5!
        // B -> - - - - - 4 -
        merger.add("A", new XmlData(5, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> - - 5 - - - 5 -
        // B -> - - - - - 4 - 5!
        merger.add("B", new XmlData(5, BigDecimal.ONE));
        Assertions.assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        Assertions.assertEquals("{ \"data\": { \"timestamp\":4, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> - - - - - - - 5 -
        // B -> - - - - - - - 5 6!
        merger.add("B", new XmlData(6, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> - - - - - - - 5 - 7!
        // B -> - - - - - - - 5 6 -
        merger.add("A", new XmlData(7, BigDecimal.ONE));
        Assertions.assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        Assertions.assertEquals("{ \"data\": { \"timestamp\":5, \"amount\":\"3.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
    }

    @Test
    public void testMerge_2() {

        // A -> 1
        // B -> -
        merger.add("A", new XmlData(1, BigDecimal.TEN));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 2
        // B -> - -
        merger.add("A", new XmlData(2, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 2 3
        // B -> - - -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 2 3 -
        // B -> - - - 3
        merger.add("B", new XmlData(3, BigDecimal.ONE));
        Assertions.assertEquals(2, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        Assertions.assertEquals("{ \"data\": { \"timestamp\":1, \"amount\":\"10.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        Assertions.assertEquals("{ \"data\": { \"timestamp\":2, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
    }

    @Test
    public void testMerge_3() {

        // A -> 5! - 6 -
        // B -> - 4 - 7
        merger.add("A", new XmlData(5, BigDecimal.TEN));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 5 - 6 -
        // B -> - 4! - 7
        merger.add("B", new XmlData(4, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 5 - 6! -
        // B -> - 4 - 7
        merger.add("A", new XmlData(6, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 5 - 6 -
        // B -> - 4 - 7!
        merger.add("B", new XmlData(7, BigDecimal.ONE));
        Assertions.assertEquals(2, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        Assertions.assertEquals("{ \"data\": { \"timestamp\":4, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        Assertions.assertEquals("{ \"data\": { \"timestamp\":5, \"amount\":\"10.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
    }

    @Test
    public void testMerge_4() {

        // A -> 3! 5 - -
        // B -> - - 2 4
        merger.add("A", new XmlData(3, BigDecimal.TEN));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 5! - -
        // B -> - - 2 4
        merger.add("A", new XmlData(5, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 5 - -
        // B -> - - 2! 4
        merger.add("B", new XmlData(2, BigDecimal.ONE));
        Assertions.assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 5 - -
        // B -> - - 2 4!
        merger.add("B", new XmlData(4, BigDecimal.ONE));
        Assertions.assertEquals(2, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        Assertions.assertEquals("{ \"data\": { \"timestamp\":2, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        Assertions.assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"10.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
    }
}