package example.producer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ProducerTest {

    private static final int TEST_SOCKET_PORT = 9999;
    private static final String TEST_XML_DATA_FILE_PATH = "test_stream.txt";

    private StreamProducer testStreamProducer() {
        XmlDataProvider xmlDataProvider = new FileDataProvider(TEST_XML_DATA_FILE_PATH);
        return new StreamProducer("test-producer", TEST_SOCKET_PORT, xmlDataProvider);
    }

    @Test
    void testProducerStartAndStreaming() {

        StreamProducer producer = testStreamProducer();
        Thread.ofVirtual().name("test-producer").start(producer::start);

        // assume producer starts in 500 millis
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        boolean receivedFinishSignal = false;
        int expectedXmlDataCount = 4;
        List<String> receivedXmlDatas = new ArrayList<>();
        try (
                Socket socket = new Socket("localhost", TEST_SOCKET_PORT);
                BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            socket.setSoTimeout(1200);
            String line;
            while ((line = fromServer.readLine()) != null) {
                if ("finish-xml-data-stream".equals(line)) {
                    receivedFinishSignal = true;
                    break;
                }
                if (receivedXmlDatas.size() > expectedXmlDataCount) {
                    break;
                }
                receivedXmlDatas.add(line);
            }

        } catch (SocketTimeoutException e) {
            Assertions.fail("expected to received xml data at most every 1.2 seconds", e);
        } catch (IOException e) {
            Assertions.fail("expected to receive all xml data messages without error", e);
        }

        Assertions.assertTrue(receivedFinishSignal, "xml data stream did not finish as expected");
        Assertions.assertEquals(expectedXmlDataCount, receivedXmlDatas.size(), "total xml data count is wrong");

        Assertions.assertEquals("<data> <timestamp>100</timestamp> <amount>3</amount> </data>",
                receivedXmlDatas.get(0), "mismatching xml data");
        Assertions.assertEquals("<data> <timestamp>101</timestamp> <amount>1.001001009</amount> </data>",
                receivedXmlDatas.get(1), "mismatching xml data");
        Assertions.assertEquals("<data> <timestamp>102</timestamp> <amount>88888.000008</amount> </data>",
                receivedXmlDatas.get(2), "mismatching xml data");
        Assertions.assertEquals("<data> <timestamp>103</timestamp> <amount>7.700000</amount> </data>",
                receivedXmlDatas.get(3), "mismatching xml data");
    }

    @Test
    void testConfigureProducerTasks() throws Exception {

        Properties testProps = new Properties();
        testProps.setProperty("producer.server.ports", "3333,4444,9999");
        testProps.setProperty("producer.xml.data.file.paths", "test_stream.txt");
        Config conf = Config.load(testProps);

        Map<String, StreamProducer> producers = StreamProducerCluster.producersFromConfig(conf);
        Assertions.assertEquals(3, producers.size(), "unexpected producer task count");

        long countOfFileProviderProducers = producers.keySet().stream().filter(it -> it.contains("source-file")).count();
        Assertions.assertEquals(1, countOfFileProviderProducers, "unexpected file sourced producer task count");
    }
}
