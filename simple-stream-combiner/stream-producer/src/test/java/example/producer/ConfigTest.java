package example.producer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
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
    void testConfigValidate() throws Exception {
        var socketPorts = List.of(1111, 2222);
        var validFile = ClassLoader.getSystemResource("test_stream.txt").getPath();
        var xmlFilePaths = List.of(validFile);
        var conf = new Config.Builder().setSocketPorts(socketPorts).setXmlDataFilePaths(xmlFilePaths).build();
        Assertions.assertEquals(2, conf.getSocketPorts().size());
        Assertions.assertEquals(1, conf.getXmlDataFilePaths().size());
    }

    @Test
    void testConfigValidate_missingPorts() {
        Assertions.assertThrows(Exception.class,
                () -> new Config.Builder().setSocketPorts(Collections.emptyList()).build(),
                "at least one stream producer port is mandatory");
    }

    @Test
    void testConfigValidate_invalidPort() {
        Assertions.assertThrows(Exception.class,
                () -> new Config.Builder().setSocketPorts(List.of(33)).build(),
                "invalid port: 33");
    }

    @Test
    void testConfigValidate_tooManyXmlFilePaths() {
        Assertions.assertThrows(Exception.class,
                () -> {
                    var socketPorts = List.of(1111);
                    var validFile_1 = ClassLoader.getSystemResource("test_stream.txt").getPath();
                    var validFile_2 = ClassLoader.getSystemResource(ConfigTest.class.getName()).getPath();
                    var xmlFilePaths = List.of(validFile_1, validFile_2);
                    new Config.Builder().setSocketPorts(socketPorts).setXmlDataFilePaths(xmlFilePaths).build();
                },
                "cannot have more xml file paths configured than producer count");
    }

    @Test
    void testConfigValidate_blankXmlFilePath() {
        Assertions.assertThrows(Exception.class,
                () -> {
                    var socketPorts = List.of(1111);
                    var xmlFilePaths = List.of(" ");
                    new Config.Builder().setSocketPorts(socketPorts).setXmlDataFilePaths(xmlFilePaths).build();
                },
                "blank xml data file path is invalid");
    }

    @Test
    void testConfigValidate_nonExistingXmlFile() {
        Assertions.assertThrows(Exception.class,
                () -> {
                    var socketPorts = List.of(1111);
                    var xmlFilePaths = List.of("/non/existing/path");
                    new Config.Builder().setSocketPorts(socketPorts).setXmlDataFilePaths(xmlFilePaths).build();
                },
                "xml data file does not exist: /non/existing/path");
    }
}
