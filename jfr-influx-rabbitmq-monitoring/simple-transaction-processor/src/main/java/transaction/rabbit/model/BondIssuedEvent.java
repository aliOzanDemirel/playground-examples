package transaction.rabbit.model;

import java.math.BigDecimal;

public class BondIssuedEvent {

    private Long bondId;
    private BigDecimal amount;
    private long timestamp;

    // some sort of partition key
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
