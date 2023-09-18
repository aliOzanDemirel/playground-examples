package example.producer;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Config {

    private static final String CONFIG_PATH_OVERRIDE_PROPERTY_KEY = "config.path.override";
    private static final String DEFAULT_CONFIG_IN_CLASSPATH = "producer.properties";

    private final List<Integer> socketPorts = new ArrayList<>();
    private final List<String> xmlDataFilePaths = new ArrayList<>();

    private Config() {
    }

    public static String configFilePath() {

        String configFilePath = System.getProperty(CONFIG_PATH_OVERRIDE_PROPERTY_KEY);
        if (configFilePath == null || configFilePath.isBlank()) {
            return DEFAULT_CONFIG_IN_CLASSPATH;
        }
        return configFilePath;
    }

    public static Config load() throws Exception {

        String configFilePath = configFilePath();
        InputStream configFileInputStream = UtilFunction.fileInputStream(configFilePath);
        Properties props = new Properties();
        props.load(configFileInputStream);
        return load(props);
    }

    public static Config load(Properties props) throws Exception {

        Config conf = new Config();
        String ports = props.getProperty("producer.server.ports");
        if (ports != null) {
            for (String portStr : ports.split(",")) {
                try {
                    int port = Integer.parseInt(portStr);
                    conf.socketPorts.add(port);
                } catch (NumberFormatException e) {
                    throw new Exception("port is not a number: " + portStr, e);
                }
            }
        }

        String xmlFilePaths = props.getProperty("producer.xml.data.file.paths");
        if (xmlFilePaths != null && !xmlFilePaths.isBlank()) {
            conf.xmlDataFilePaths.addAll(Arrays.asList(xmlFilePaths.split(",")));
        }
        return conf;
    }

    public void validate() throws Exception {

        if (socketPorts.isEmpty()) {
            throw new Exception("at least one stream producer port is mandatory");
        }

        if (xmlDataFilePaths.size() > socketPorts.size()) {
            throw new Exception("cannot have more xml file paths configured than producer count");
        }

        for (Integer port : socketPorts) {
            if (port < 1024 || port > 65535) {
                throw new Exception("invalid port: " + port);
            }
        }

        for (String xmlDataFilePath : xmlDataFilePaths) {
            if (xmlDataFilePath.isBlank()) {
                throw new Exception("blank xml data file path is invalid");
            }
            if (!Files.exists(Path.of(xmlDataFilePath))) {
                throw new Exception("xml data file does not exist: " + xmlDataFilePath);
            }
        }
    }

    public List<Integer> getSocketPorts() {
        return socketPorts;
    }

    public List<String> getXmlDataFilePaths() {
        return xmlDataFilePaths;
    }
}
