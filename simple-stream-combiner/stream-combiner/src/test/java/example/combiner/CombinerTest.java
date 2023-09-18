package example.combiner;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static example.Log.logErr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class CombinerTest {

    private StreamCombiner testStreamCombiner(List<Integer> ports, JsonOutput.BufferWriter writer) throws Exception {
        var merger = new StreamMerger4(1000, writer);
        var mapper = new XmlMapper();
        Map<String, StreamConsumer> consumers = new HashMap<>();
        for (Integer port : ports) {
            var target = new ProducerTarget("localhost:" + port);
            var name = "test-consumer-" + port;
            var testConsumer = new StreamConsumer(name, target, Duration.ofSeconds(10), mapper, merger);
            consumers.put(name, testConsumer);
        }
        return new StreamCombiner(consumers, merger);
    }

    private void startTestProducer(List<String> xmlRecords, int port) {
        var name = "test-producer-" + port;
        Thread.ofVirtual().name(name).start(() -> {
            try (
                    ServerSocket serverSocket = new ServerSocket(port);
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
            ) {
                xmlRecords.forEach(xmlString -> {
                    toClient.println(xmlString);
                    toClient.flush();
                });
            } catch (Exception e) {
                logErr(e, "[%s] failed test producer", name);
            }
        });
    }

    @Test
    void testStartCombiner() throws Exception {

        // start some test producers
        startTestProducer(List.of(
                "<data> <timestamp>4</timestamp> <amount>4.0</amount> </data>",
                "<data> <timestamp>5</timestamp> <amount>50.0</amount> </data>",
                "<data> <timestamp>8</timestamp> <amount>8.0</amount> </data>",
                "<data> <timestamp>9</timestamp> <amount>90.0</amount> </data>"
        ), 9999);
        startTestProducer(List.of(
                "<data> <timestamp>3</timestamp> <amount>3.0</amount> </data>",
                "<data> <timestamp>5</timestamp> <amount>50.0</amount> </data>",
                "<data> <timestamp>6</timestamp> <amount>6.0</amount> </data>",
                "<data> <timestamp>9</timestamp> <amount>90.0</amount> </data>",
                "<data> <timestamp>15</timestamp> <amount>15.0</amount> </data>"
        ), 8888);

        // assume producers start in 100 millis
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }

        // then start consumer tasks to consume from test producers
        var writer = new JsonOutput.BufferWriter();
        var streamCombiner = testStreamCombiner(List.of(8888, 9999), writer);
        streamCombiner.start();

        // timeout for everything to start and done processing all records
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        List<String> expectedJsonRecords = List.of(
                "{ \"data\": { \"timestamp\":3, \"amount\":\"3.000000\" }}",
                "{ \"data\": { \"timestamp\":4, \"amount\":\"4.000000\" }}",
                "{ \"data\": { \"timestamp\":5, \"amount\":\"100.000000\" }}",
                "{ \"data\": { \"timestamp\":6, \"amount\":\"6.000000\" }}",
                "{ \"data\": { \"timestamp\":8, \"amount\":\"8.000000\" }}",
                "{ \"data\": { \"timestamp\":9, \"amount\":\"180.000000\" }}",
                "{ \"data\": { \"timestamp\":15, \"amount\":\"15.000000\" }}"
        );
        List<String> receivedRecords = writer.getXmlDataJsons();
        assertEquals(expectedJsonRecords.size(), receivedRecords.size(), "total received xml data count is wrong");
        assertIterableEquals(expectedJsonRecords, receivedRecords, "expected json data does not match");
    }

    @Test
    void testConfigureConsumer() throws Exception {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "some_other_host:8888,localhost:9999");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "25");
        testProps.setProperty("combiner.buffer.capacity", "1234");
        Config conf = Config.load(testProps);

        Map<String, StreamConsumer> consumers = StreamCombiner.consumersFromConfig(conf, null, new StreamMerger4(1234, null));
        assertEquals(2, consumers.size(), "unexpected consumer task count");
    }
}
