package bond.messaging;

import bond.domain.Bond;

import java.math.BigDecimal;

public class BondIssuedEvent {

    public BondIssuedEvent() {
    }

    public BondIssuedEvent(Bond bond) {
        bondId = bond.getId();
        amount = bond.getAmount();
        timestamp = System.currentTimeMillis();
    }

    private Long bondId;
    private BigDecimal amount;
    private long timestamp;

    public Long getBondId() {
        return bondId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "BondIssuedEvent{" +
                "bondId=" + bondId +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
