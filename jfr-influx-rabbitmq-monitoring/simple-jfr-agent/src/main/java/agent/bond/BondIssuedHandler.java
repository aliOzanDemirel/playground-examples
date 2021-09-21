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

        if (event.hasField("timestampInUtc")) {

            long timestampInUtc = event.getLong("timestampInUtc");

            long clientId = NUMERIC_UNKNOWN_ID;
            if (event.hasField("clientId")) {
                clientId = event.getLong("clientId");
            }

            BigDecimal amount = null;
            if (event.hasField("amount")) {
                String amountText = event.getValue("amount");
                amount = new BigDecimal(amountText);
            }

            writer.writeBondIssuedEvent(clientId, amount, timestampInUtc);

        } else {

            // record unknown if time is not stamped to recorded event
            writer.writeBondIssuedEvent(NUMERIC_UNKNOWN_ID, null, NUMERIC_UNKNOWN_ID);
        }
    }
}
