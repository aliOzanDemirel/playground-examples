package agent.bond;

import agent.util.EventUtil;
import jdk.jfr.consumer.RecordedEvent;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static agent.util.EventUtil.NUMERIC_UNKNOWN_ID;

public class BondIssuedHandler implements Consumer<RecordedEvent> {

    private final BondIssuerMetricWriter writer = new BondIssuerMetricWriter();

    @Override
    public void accept(RecordedEvent event) {

        EventUtil.logEventDuration(event, getClass());

        long clientId = NUMERIC_UNKNOWN_ID;
        if (event.hasField("clientId")) {
            clientId = event.getValue("clientId");
        }
        BigDecimal amount = null;
        if (event.hasField("amount")) {
            amount = event.getValue("amount");
        }
        long timestampInUtc = NUMERIC_UNKNOWN_ID;
        if (event.hasField("timestampInUtc")) {
            timestampInUtc = event.getValue("timestampInUtc");
        }

        writer.writeBondIssuedEvent(clientId, amount, timestampInUtc);
    }
}
