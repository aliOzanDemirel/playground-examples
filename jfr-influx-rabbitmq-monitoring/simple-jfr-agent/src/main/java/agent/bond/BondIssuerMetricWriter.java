package agent.bond;

import agent.influx.InfluxWriter;
import org.influxdb.dto.Point;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BondIssuerMetricWriter {

    private static final String BOND_MEASUREMENT_NAME = "bonds";

    public void writeBondIssuedEvent(long clientId, BigDecimal amount, long timestampInUtc) {

        String amountStr = amount == null ? "unknown" : amount.setScale(4, RoundingMode.HALF_UP).toString();
        String clientIdStr = clientId == -1 ? "unknown" : String.valueOf(clientId);

        Point.Builder dataPointBuilder = Point.measurement(BOND_MEASUREMENT_NAME)
                .addField("issued_timestamp", timestampInUtc)
                .addField("amount", amountStr)
                .tag("client_id", clientIdStr);

        InfluxWriter.getWriter().writeDataPoint(dataPointBuilder);
    }
}
