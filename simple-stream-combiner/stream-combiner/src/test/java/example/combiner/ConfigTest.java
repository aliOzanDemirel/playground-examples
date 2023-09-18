package example.combiner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {

    @Test
    void testConfigLoad_default() throws Exception {

        Config conf = Config.load();
        assertEquals(1, conf.getProducers().size(), "unexpected producer count");
        assertEquals(9999, conf.getProducers().get(0).getPort(), "unexpected producer port");
        assertEquals("localhost", conf.getProducers().get(0).getHost(), "unexpected producer host");

        assertEquals(5, conf.getSocketReceiveTimeout().toSeconds(), "unexpected socket receive timeout");
        assertEquals(999_888, conf.getCombinerMergeBufferCapacity(), "unexpected buffer capacity");
    }

    @Test
    void testConfig_valid() throws Exception {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "someHost:5555");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "10");
        testProps.setProperty("combiner.buffer.capacity", "100");

        Config conf = Config.load(testProps);
        assertEquals(1, conf.getProducers().size(), "unexpected producer count");
        assertEquals(5555, conf.getProducers().get(0).getPort(), "unexpected producer port");
        assertEquals("someHost", conf.getProducers().get(0).getHost(), "unexpected producer host");

        assertEquals(10, conf.getSocketReceiveTimeout().toSeconds(), "unexpected socket receive timeout");
        assertEquals(100, conf.getCombinerMergeBufferCapacity(), "unexpected buffer capacity");
    }

    @Test
    void testConfigValidate_emptyProducerConfig() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "20");
        testProps.setProperty("combiner.buffer.capacity", "20");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "missing 'combiner.producers'");
    }

    @Test
    void testConfigValidate_missingProducerConfig() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "20");
        testProps.setProperty("combiner.buffer.capacity", "20");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "missing 'combiner.producers'");
    }

    @Test
    void testConfigValidate_negativeTimeout() throws Exception {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "someHost:5555");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "-1");
        testProps.setProperty("combiner.buffer.capacity", "100");
        Config conf = Config.load(testProps);

        Assertions.assertThrows(Exception.class, conf::validate,
                "timeout cannot be less than 0 -> -1000");
    }

    @Test
    void testConfigValidate_negativeBufferCapacity() throws Exception {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "someHost:5555");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "20");
        testProps.setProperty("combiner.buffer.capacity", "-1");
        Config conf = Config.load(testProps);

        Assertions.assertThrows(Exception.class, conf::validate,
                "buffer capacity cannot be less than 0 -> -1");
    }

    @Test
    void testConfigLoad_invalid_missingProducer() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "10");
        testProps.setProperty("combiner.buffer.capacity", "100");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "missing 'combiner.producers'");
    }

    @Test
    void testConfigLoad_invalid_missingTimeout() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "someHost:5555");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "");
        testProps.setProperty("combiner.buffer.capacity", "100");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "missing 'combiner.socket.receive.timeout.seconds'");
    }

    @Test
    void testConfigLoad_invalid_nonNumberTimeout() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "someHost:5555");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "notNumber");
        testProps.setProperty("combiner.buffer.capacity", "100");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "'combiner.socket.receive.timeout.seconds' is not a number: notNumber");
    }

    @Test
    void testConfigLoad_invalid_missingBufferCapacity() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "someHost:5555");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "100");
        testProps.setProperty("combiner.buffer.capacity", "");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "missing 'combiner.buffer.capacity'");
    }

    @Test
    void testConfigLoad_invalid_nonNumberBufferCapacity() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "someHost:5555");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "44");
        testProps.setProperty("combiner.buffer.capacity", "notNumber");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "'combiner.buffer.capacity' is not a number: notNumber");
    }

    @Test
    void testConfig_invalidProducer_noHostPort() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "noHostPortPair");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "expected to find host:port parts in 'noHostPortPair'");
    }

    @Test
    void testConfig_invalidProducer_noHost() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", ":1023");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "blank host is invalid");
    }

    @Test
    void testConfig_invalidProducer_nonAllowedPort() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "localhost:1023");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "invalid port -> 1023");
    }

    @Test
    void testConfig_invalidProducer_notNumberPort() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "localhost:notNumber");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "port is not a number -> notNumber");
    }
}
