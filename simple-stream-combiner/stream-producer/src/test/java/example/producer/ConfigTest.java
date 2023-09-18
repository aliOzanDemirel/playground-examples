package example.producer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigTest {

    @Test
    void testConfigLoad_default() throws Exception {

        Config conf = Config.load();
        assertEquals(1, conf.getSocketPorts().size(), "unexpected socket port count");
        assertEquals(9999, conf.getSocketPorts().get(0), "unexpected socket port");
        assertTrue(conf.getXmlDataFilePaths().isEmpty(), "unexpected xml data file path");
    }

    @Test
    void testConfigLoad_defaultWithExplicitConfig() throws Exception {

        System.setProperty("config.path.override", "producer.properties");
        Config conf = Config.load();
        assertEquals(1, conf.getSocketPorts().size(), "unexpected socket port count");
        assertEquals(9999, conf.getSocketPorts().get(0), "unexpected socket port");
        assertTrue(conf.getXmlDataFilePaths().isEmpty(), "unexpected xml data file path");
    }

    @Test
    void testConfigLoad_customProps() throws Exception {

        Properties testProps = new Properties();
        testProps.setProperty("producer.server.ports", "1111,2222");
        testProps.setProperty("producer.xml.data.file.paths", "/some/file/path,/another/path");

        Config conf = Config.load(testProps);
        assertEquals(2, conf.getSocketPorts().size(), "unexpected socket port count");
        assertEquals(1111, conf.getSocketPorts().get(0), "unexpected socket port");
        assertEquals(2222, conf.getSocketPorts().get(1), "unexpected socket port");

        assertEquals(2, conf.getXmlDataFilePaths().size(), "unexpected xml data file count");
        assertEquals("/some/file/path", conf.getXmlDataFilePaths().get(0), "unexpected xml data file");
        assertEquals("/another/path", conf.getXmlDataFilePaths().get(1), "unexpected xml data file");
    }

    @Test
    void testConfigLoad_nonNumberPort() {

        Properties testProps = new Properties();
        testProps.setProperty("producer.server.ports", "somethingNotNumber");

        Assertions.assertThrows(Exception.class, () -> Config.load(testProps),
                "port is not a number: somethingNotNumber");
    }

    @Test
    void testConfig_invalid_invalidPort() throws Exception {

        Properties testProps = new Properties();
        testProps.setProperty("producer.server.ports", "33");
        testProps.setProperty("producer.xml.data.file.paths", "/can/have/one/path");

        Config conf = Config.load(testProps);
        Assertions.assertThrows(Exception.class, conf::validate,
                "invalid port: 33");
    }

    @Test
    void testConfig_invalid_tooManyXmlFilePaths() throws Exception {

        Properties testProps = new Properties();
        testProps.setProperty("producer.server.ports", "1111");
        testProps.setProperty("producer.xml.data.file.paths", "/can/have/one/path,/cannot/have/second/path");

        Config conf = Config.load(testProps);
        Assertions.assertThrows(Exception.class, conf::validate,
                "cannot have more xml file paths configured than producer count");
    }
}
