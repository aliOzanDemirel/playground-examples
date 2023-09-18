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
    }

    @Test
    void testConfig_valid() throws Exception {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "someHost:5555");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "10");

        Config conf = Config.load(testProps);
        assertEquals(1, conf.getProducers().size(), "unexpected producer count");
        assertEquals(5555, conf.getProducers().get(0).getPort(), "unexpected producer port");
        assertEquals("someHost", conf.getProducers().get(0).getHost(), "unexpected producer host");

        assertEquals(10, conf.getSocketReceiveTimeout().toSeconds(), "unexpected socket receive timeout");
    }

    @Test
    void testConfig_invalid_negativeTimeout() throws Exception {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "someHost:5555");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "-1");

        Config conf = Config.load(testProps);
        Assertions.assertThrows(Exception.class, conf::validate,
                "timeout cannot be less than 0 -> -1000");
    }

    @Test
    void testConfig_invalid_nonNumberTimeout() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "someHost:5555");
        testProps.setProperty("combiner.socket.receive.timeout.seconds", "notNumber");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "timeout is not a number: notNumber");
    }

    @Test
    void testConfig_invalid_missingProducer() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "missing 'combiner.producers'");
    }

    @Test
    void testConfig_invalid_invalidProducer() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "noHostPortPair");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "expected to find host:port parts in 'noHostPortPair'");
    }

    @Test
    void testConfig_invalid_invalidHost() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", ":1023");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "blank host is invalid");
    }

    @Test
    void testConfig_invalid_invalidPort() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "localhost:1023");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "invalid port -> 1023");
    }

    @Test
    void testConfig_invalid_portNotNumber() {

        Properties testProps = new Properties();
        testProps.setProperty("combiner.producers", "localhost:notNumber");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "port is not a number -> notNumber");
    }
}
