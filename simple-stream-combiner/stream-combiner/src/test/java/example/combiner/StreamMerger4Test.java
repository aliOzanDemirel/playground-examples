package example.combiner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StreamMerger4Test {

    private StreamMerger4 merger;
    private JsonOutput.BufferWriter writer;

    @BeforeEach
    void beforeEach() {
        writer = new JsonOutput.BufferWriter();
        merger = new StreamMerger4(1000, writer);
        merger.registerStream("A");
        merger.registerStream("B");
    }

    @Test
    void testMerge_bufferOverCapacity() {

        merger = new StreamMerger4(1, writer);
        merger.registerStream("A");
        merger.registerStream("B");

        try {
            merger.add("A", new XmlData(1, BigDecimal.ONE));
            merger.add("A", new XmlData(1, BigDecimal.TWO));
            merger.add("A", new XmlData(2, BigDecimal.TEN));
        } catch (StreamMerger4.BufferOverCapacityException e) {
            Assertions.fail("should not fail to add records for single timestamp", e);
        }
        Assertions.assertThrows(StreamMerger4.BufferOverCapacityException.class,
                () -> merger.add("A", new XmlData(3, BigDecimal.TEN)));
    }

    @Test
    void testMerge_deregisterStream_1() throws StreamMerger4.BufferOverCapacityException {

        merger.registerStream("C");

        // A -> 1
        // B -> -
        // C -> -
        merger.add("A", new XmlData(1, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 -
        // B -> - 2
        // C -> - -
        merger.add("B", new XmlData(2, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        merger.deregisterStream("C");
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":1, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();
    }

    @Test
    void testMerge_deregisterStream_2() throws StreamMerger4.BufferOverCapacityException {

        merger.registerStream("C");

        // A -> 1
        // B -> -
        // C -> -
        merger.add("A", new XmlData(1, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 -
        // B -> - 2
        // C -> - -
        merger.add("B", new XmlData(2, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 - -
        // B -> - 2 -
        // C -> - - 5
        merger.add("C", new XmlData(5, BigDecimal.TWO));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":1, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        merger.deregisterStream("C");
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 - -
        // B -> - 2 4
        merger.add("B", new XmlData(4, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 - - 4
        // B -> - 2 4 -
        merger.add("A", new XmlData(4, BigDecimal.TWO));
        assertEquals(2, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":2, \"amount\":\"2.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        assertEquals("{ \"data\": { \"timestamp\":4, \"amount\":\"4.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
        writer.getXmlDataJsons().clear();
    }

    @Test
    void testMerge_shouldNotPushUntilAllConsumersEmitted() throws StreamMerger4.BufferOverCapacityException {

        merger.registerStream("C");

        // A -> 3
        // B -> -
        // C -> -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 4
        // B -> - -
        // C -> - -
        merger.add("A", new XmlData(4, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 4 5
        // B -> - - -
        // C -> - - -
        merger.add("A", new XmlData(5, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 4 5 -
        // B -> - - - 1
        // C -> - - - -
        merger.add("B", new XmlData(1, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");
    }

    @Test
    void testMerge_determineToPush_1() throws StreamMerger4.BufferOverCapacityException {

        // A -> 3
        // B -> -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 -
        // B -> - 3
        merger.add("B", new XmlData(3, BigDecimal.TEN));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"11.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
    }

    @Test
    void testMerge_determineToPush_2() throws StreamMerger4.BufferOverCapacityException {

        // A -> 3
        // B -> -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 -
        // B -> - 5
        merger.add("B", new XmlData(5, BigDecimal.TEN));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
    }

    @Test
    void testMerge_determineToPush_3() throws StreamMerger4.BufferOverCapacityException {

        // A -> 3
        // B -> -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 -
        // B -> - 2
        merger.add("B", new XmlData(2, BigDecimal.TEN));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":2, \"amount\":\"10.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
    }

    @Test
    void testMerge_pushBuffered_1() throws StreamMerger4.BufferOverCapacityException {

        // A -> 3
        // B -> -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 4
        // B -> - -
        merger.add("A", new XmlData(4, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 4 -
        // B -> - - 2
        merger.add("B", new XmlData(2, BigDecimal.TEN));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":2, \"amount\":\"10.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
    }

    @Test
    void testMerge_pushBuffered_2() throws StreamMerger4.BufferOverCapacityException {

        // A -> 3
        // B -> -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 4
        // B -> - -
        merger.add("A", new XmlData(4, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 4 -
        // B -> - - 5
        merger.add("B", new XmlData(5, BigDecimal.TEN));
        assertEquals(2, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        assertEquals("{ \"data\": { \"timestamp\":4, \"amount\":\"2.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
    }

    @Test
    void testMerge_pushBuffered_3() throws StreamMerger4.BufferOverCapacityException {

        // A -> 3
        // B -> -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 5
        // B -> - -
        merger.add("A", new XmlData(5, BigDecimal.TEN));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 5 -
        // B -> - - 4
        merger.add("B", new XmlData(4, BigDecimal.TWO));
        assertEquals(2, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        assertEquals("{ \"data\": { \"timestamp\":4, \"amount\":\"2.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
    }

    @Test
    void testMerge_pushBuffered_4() throws StreamMerger4.BufferOverCapacityException {

        // A -> 3
        // B -> -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 7
        // B -> - -
        merger.add("A", new XmlData(7, BigDecimal.TEN));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 7 -
        // B -> - - 4
        merger.add("B", new XmlData(4, BigDecimal.TWO));
        assertEquals(2, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        assertEquals("{ \"data\": { \"timestamp\":4, \"amount\":\"2.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
    }

    @Test
    void testMerge_pushBuffered_5() throws StreamMerger4.BufferOverCapacityException {

        // A -> 3
        // B -> -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 5
        // B -> - -
        merger.add("A", new XmlData(5, BigDecimal.TEN));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 5 -
        // B -> - - 3
        merger.add("B", new XmlData(3, BigDecimal.TWO));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"3.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
    }

    @Test
    void testMerge_pushBuffered_6() throws StreamMerger4.BufferOverCapacityException {

        // A -> 3
        // B -> -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 5
        // B -> - -
        merger.add("A", new XmlData(5, BigDecimal.TEN));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");
        // A -> 3 5 6
        // B -> - - -
        merger.add("A", new XmlData(6, BigDecimal.TEN));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 3 5 6 -
        // B -> - - - 5
        merger.add("B", new XmlData(5, BigDecimal.TWO));
        assertEquals(2, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        assertEquals("{ \"data\": { \"timestamp\":5, \"amount\":\"12.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
    }

    @Test
    void testMerge_scenario_1() throws StreamMerger4.BufferOverCapacityException {

        // A -> 1
        // B -> -
        merger.add("A", new XmlData(1, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 -
        // B -> - 2
        merger.add("B", new XmlData(2, BigDecimal.TEN));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":1, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> 1 - 3
        // B -> - 2 -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":2, \"amount\":\"10.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> 1 - 3 -
        // B -> - 2 - 3
        merger.add("B", new XmlData(3, BigDecimal.TEN));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"11.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> 1 - 3 - -
        // B -> - 2 - 3 8
        merger.add("B", new XmlData(8, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 - 3 - - 4
        // B -> - 2 - 3 8 -
        merger.add("A", new XmlData(4, BigDecimal.TWO));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":4, \"amount\":\"2.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> 1 - 3 - - 4 -
        // B -> - 2 - 3 8 - 9
        merger.add("B", new XmlData(9, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 - 3 - - 4 - 5
        // B -> - 2 - 3 8 - 9 -
        merger.add("A", new XmlData(5, BigDecimal.ONE));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":5, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> 1 - 3 - - 4 - 5 9
        // B -> - 2 - 3 8 - 9 - -
        merger.add("A", new XmlData(9, BigDecimal.TEN));
        assertEquals(2, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":8, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        assertEquals("{ \"data\": { \"timestamp\":9, \"amount\":\"12.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
        writer.getXmlDataJsons().clear();
    }

    @Test
    void testMerge_scenario_2() throws StreamMerger4.BufferOverCapacityException {

        merger.registerStream("C");

        // A -> 1
        // B -> -
        // C -> -
        merger.add("A", new XmlData(1, BigDecimal.TEN));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 2
        // B -> - -
        // C -> - -
        merger.add("A", new XmlData(2, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 2 3
        // B -> - - -
        // C -> - - -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 2 3 -
        // B -> - - - 3
        // C -> - - - -
        merger.add("B", new XmlData(3, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 2 3 - -
        // B -> - - - 3 -
        // C -> - - - - 7
        merger.add("C", new XmlData(7, BigDecimal.ONE));
        assertEquals(3, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":1, \"amount\":\"10.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        assertEquals("{ \"data\": { \"timestamp\":2, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
        assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"3.000000\" }}", writer.getXmlDataJsons().get(2),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> 1` 2` 3` - -
        // B -> - - - 3` - 7
        // C -> - - - -  7 -
        merger.add("B", new XmlData(7, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1` 2` 3` - - 8
        // B -> - - - 3` - 7 -
        // C -> - - - -  7 - -
        merger.add("A", new XmlData(8, BigDecimal.TWO));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":7, \"amount\":\"3.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();
    }

    @Test
    void testMerge_scenario_3() throws StreamMerger4.BufferOverCapacityException {

        merger.registerStream("C");
        merger.registerStream("D");
        merger.registerStream("E");

        // A -> 1
        // B -> -
        // C -> -
        // D -> -
        // E -> -
        merger.add("A", new XmlData(1, BigDecimal.TEN));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 -
        // B -> - 7
        // C -> - -
        // D -> - -
        // E -> - -
        merger.add("B", new XmlData(7, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 - 3
        // B -> - 7 -
        // C -> - - -
        // D -> - - -
        // E -> - - -
        merger.add("A", new XmlData(3, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 - 3 -
        // B -> - 7 - -
        // C -> - - - 2
        // D -> - - - -
        // E -> - - - -
        merger.add("C", new XmlData(2, BigDecimal.TWO));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 - 3 - -
        // B -> - 7 - - -
        // C -> - - - 2 -
        // D -> - - - - 8
        // E -> - - - - -
        merger.add("D", new XmlData(8, BigDecimal.ONE));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 - 3 - - -
        // B -> - 7 - - - -
        // C -> - - - 2 - -
        // D -> - - - - 8 -
        // E -> - - - - - 9
        merger.add("E", new XmlData(9, BigDecimal.TEN));
        assertEquals(2, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":1, \"amount\":\"10.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        assertEquals("{ \"data\": { \"timestamp\":2, \"amount\":\"2.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> 1 - 3 - - - -
        // B -> - 7 - - - - -
        // C -> - - - 2 - - 9
        // D -> - - - - 8 - -
        // E -> - - - - - 9 -
        merger.add("C", new XmlData(9, BigDecimal.TEN));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":3, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> 1 - 3 - - - - -
        // B -> - 7 - - - - - 9
        // C -> - - - 2 - - 9 -
        // D -> - - - - 8 - - -
        // E -> - - - - - 9 - -
        merger.add("B", new XmlData(9, BigDecimal.TEN));
        assertEquals(0, writer.getXmlDataJsons().size(), "should not have pushed any xml data");

        // A -> 1 - 3 - - - - - 9
        // B -> - 7 - - - - - 9 -
        // C -> - - - 2 - - 9 - -
        // D -> - - - - 8 - - - -
        // E -> - - - - - 9 - - -
        merger.add("A", new XmlData(9, BigDecimal.TEN));
        assertEquals(2, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":7, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        assertEquals("{ \"data\": { \"timestamp\":8, \"amount\":\"1.000000\" }}", writer.getXmlDataJsons().get(1),
                "xml data does not match");
        writer.getXmlDataJsons().clear();

        // A -> 1 - 3 - - - - - 9 -
        // B -> - 7 - - - - - 9 - -
        // C -> - - - 2 - - 9 - - -
        // D -> - - - - 8 - - - - 9
        // E -> - - - - - 9 - - - -
        merger.add("D", new XmlData(9, BigDecimal.TEN));
        assertEquals(1, writer.getXmlDataJsons().size(), "unexpected count of xml data");
        assertEquals("{ \"data\": { \"timestamp\":9, \"amount\":\"50.000000\" }}", writer.getXmlDataJsons().get(0),
                "xml data does not match");
        writer.getXmlDataJsons().clear();
    }
}
