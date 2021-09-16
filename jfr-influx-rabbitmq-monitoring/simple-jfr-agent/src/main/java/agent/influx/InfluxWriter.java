package agent.influx;

import agent.util.RandomData;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class InfluxWriter {

    private static final String DEFAULT_DB_NAME = "grafana_exposed_metrics";
    private static final String DEFAULT_RETENTION_NAME = "retention_10_hours";

    private static final Logger log = LoggerFactory.getLogger(InfluxWriter.class);
    private static final InfluxWriter writer = new InfluxWriter();

    private final InfluxDB influxDbClient;

    private InfluxWriter() {
        influxDbClient = init();
    }

    private InfluxDB init() {

        try {
            String host = InfluxConfig.getHost();
            String port = InfluxConfig.getPort();
            String username = InfluxConfig.getUsername();
            String password = InfluxConfig.getPassword();

            String serverURL = "http://" + host + ":" + port;
            log.info("Initializing InfluxDB client, server: " + serverURL + " user: " + username);

            InfluxDB influxDB = InfluxDBFactory.connect(serverURL, username, password);
            influxDB.query(new Query("CREATE DATABASE " + DEFAULT_DB_NAME));
            influxDB.setDatabase(DEFAULT_DB_NAME);

            influxDB.query(new Query("CREATE RETENTION POLICY "
                    + DEFAULT_RETENTION_NAME + " ON " + DEFAULT_DB_NAME
                    + " DURATION 10h REPLICATION 1 DEFAULT"));
            influxDB.setRetentionPolicy(DEFAULT_RETENTION_NAME);
            influxDB.enableBatch(BatchOptions.DEFAULTS);

            Runtime.getRuntime().addShutdownHook(new Thread(influxDB::close));
            return influxDB;

        } catch (Exception e) {

            log.error("Could not initialize influxdb client: " + e.getMessage());
            return null;
        }
    }

    public static InfluxWriter getWriter() {
        return writer;
    }

    public static boolean isWriterConfigured() {
        return writer.influxDbClient != null;
    }

    public void writeDataPoint(Point.Builder dataPointBuilder) {

        var someCloudRegion = RandomData.dummyRegion();
        var somePartitionIdentifier = RandomData.dummyPartition();

        var dataPoint = dataPointBuilder
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("region", someCloudRegion)
                .tag("service_partition", somePartitionIdentifier)
                .build();

        try {
            log.info("Writing to InfluxDB: {}", dataPoint);
            influxDbClient.write(dataPoint);

        } catch (Exception e) {

            log.error("Error occurred while writing data point to InfluxDB!", e);
        }
    }
}
