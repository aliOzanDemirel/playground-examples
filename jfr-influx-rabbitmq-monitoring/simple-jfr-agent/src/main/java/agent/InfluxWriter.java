package agent;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class InfluxWriter {

    private static final String DEFAULT_DB_NAME = "grafana_exposed_metrics";
    private static final String DEFAULT_RETENTION_NAME = "retention_10_hours";
    private static final String DEFAULT_MEASUREMENT_NAME = "clothing_reviews";

    private static final Logger javaLog = Logger.getLogger("InfluxWriter");
    private final InfluxDB influxDbClient;

    public InfluxWriter() {
        influxDbClient = init();
    }

    private InfluxDB init() {

        try {
            String host = System.getenv("INFLUX_HOST");
            String port = System.getenv("INFLUX_PORT");
            String username = System.getenv("INFLUX_USER");
            String password = System.getenv("INFLUX_PASS");

            if (host == null || port == null || username == null || password == null) {
                host = "127.0.0.1";
                port = "8086";
                username = "root";
                password = "root";
            }
            String serverURL = "http://" + host + ":" + port;
            javaLog.info("Initializing InfluxDB client, server: " + serverURL + " user: " + username);
            InfluxDB influxDB = InfluxDBFactory.connect(serverURL, username, password);

            influxDB.query(new Query("CREATE DATABASE " + DEFAULT_DB_NAME));
            influxDB.setDatabase(DEFAULT_DB_NAME);

            influxDB.query(new Query("CREATE RETENTION POLICY " + DEFAULT_RETENTION_NAME
                    + " ON " + DEFAULT_DB_NAME + " DURATION 10h REPLICATION 1 DEFAULT"));
            influxDB.setRetentionPolicy(DEFAULT_RETENTION_NAME);

            influxDB.enableBatch(BatchOptions.DEFAULTS);

            Runtime.getRuntime().addShutdownHook(new Thread(influxDB::close));
            return influxDB;

        } catch (Exception e) {
            javaLog.severe("Error occurred while initializing influxdb client: " + e.getMessage());
            return null;
        }
    }

    public void writeReviewAddedEvent(int isSuccess, int rating, String clothingBrand) {

        if (influxDbClient == null) {
            javaLog.warning("Influx DB client is not initialized!");
            return;
        }

        String brand = clothingBrand == null ? "unknown" : clothingBrand;
        String someCloudRegion = RandomData.dummyRegion();
        String somePartitionIdentifier = RandomData.dummyPartition();

        String requestStatus = "unknown";
        if (isSuccess == 1) {
            requestStatus = "success";
        } else if (isSuccess == 0) {
            requestStatus = "failure";
        }

        Point dataPoint = Point.measurement(DEFAULT_MEASUREMENT_NAME)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)

                // -1 unknown
                .addField("rating", rating)

                .tag("request_status", requestStatus)
                .tag("brand", brand)
                .tag("region", someCloudRegion)
                .tag("service_partition", somePartitionIdentifier)
                .build();

        try {
            javaLog.info("Writing datapoint -> " + dataPoint);
            influxDbClient.write(dataPoint);
        } catch (Exception e) {
            javaLog.severe("Error occurred while saving data point: " + e.getMessage());
        }
    }
}
