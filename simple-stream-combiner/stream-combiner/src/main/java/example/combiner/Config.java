package example.combiner;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Config {

    private static final String CONFIG_PATH_OVERRIDE_PROPERTY_KEY = "config.path.override";
    private static final String DEFAULT_CONFIG_IN_CLASSPATH = "combiner.properties";

    private int combinerMergeBufferCapacity;
    private Duration socketReceiveTimeout;
    private final List<ProducerTarget> producers = new ArrayList<>();

    private Config() {
    }

    public static Config load() throws Exception {

        String configFilePath = System.getProperty(CONFIG_PATH_OVERRIDE_PROPERTY_KEY);
        if (configFilePath == null || configFilePath.isBlank()) {

            InputStream defaultConfigStream = ClassLoader.getSystemResourceAsStream(DEFAULT_CONFIG_IN_CLASSPATH);
            if (defaultConfigStream == null) {
                throw new Exception("could not find config file -> " + DEFAULT_CONFIG_IN_CLASSPATH);
            }
            return load(defaultConfigStream);
        }
        return load(new FileInputStream(configFilePath));
    }

    public static Config load(InputStream configFileInputStream) throws Exception {

        Properties props = new Properties();
        props.load(configFileInputStream);
        return load(props);
    }

    public static Config load(Properties props) throws Exception {

        String producersStr = props.getProperty("combiner.producers");
        if (producersStr == null || producersStr.isBlank()) {
            throw new Exception("missing 'combiner.producers'");
        }

        Config conf = new Config();
        String[] producers = producersStr.split(",");
        for (String producer : producers) {
            conf.producers.add(new ProducerTarget(producer));
        }

        String timeoutStr = props.getProperty("combiner.socket.receive.timeout.seconds");
        if (timeoutStr == null || timeoutStr.isBlank()) {
            throw new Exception("missing 'combiner.socket.receive.timeout.seconds'");
        }
        try {
            long seconds = Long.parseLong(timeoutStr);
            conf.socketReceiveTimeout = Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            String errMsg = String.format("'combiner.socket.receive.timeout.seconds' is not a number -> %s", timeoutStr);
            throw new Exception(errMsg, e);
        }

        String bufferCapacity = props.getProperty("combiner.buffer.capacity");
        if (bufferCapacity == null || bufferCapacity.isBlank()) {
            throw new Exception("missing 'combiner.buffer.capacity'");
        }
        try {
            conf.combinerMergeBufferCapacity = Integer.parseInt(bufferCapacity);
        } catch (NumberFormatException e) {
            String errMsg = String.format("'combiner.buffer.capacity' is not a number -> %s", timeoutStr);
            throw new Exception(errMsg, e);
        }

        return conf;
    }

    public void validate() throws Exception {

        if (producers.isEmpty()) {
            throw new Exception("no producer is configured!");
        }

        if (socketReceiveTimeout.toMillis() < 0) {
            throw new Exception(String.format("timeout cannot be less than 0 -> %d", socketReceiveTimeout.toMillis()));
        }

        if (combinerMergeBufferCapacity < 0) {
            throw new Exception(String.format("buffer capacity cannot be less than 0 -> %d", combinerMergeBufferCapacity));
        }
    }

    public int getCombinerMergeBufferCapacity() {
        return combinerMergeBufferCapacity;
    }

    public Duration getSocketReceiveTimeout() {
        return socketReceiveTimeout;
    }

    public List<ProducerTarget> getProducers() {
        return producers;
    }
}
