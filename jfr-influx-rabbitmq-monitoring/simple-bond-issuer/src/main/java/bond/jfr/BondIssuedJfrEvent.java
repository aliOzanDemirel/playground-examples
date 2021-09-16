package bond.jfr;

import jdk.jfr.*;

import java.math.BigDecimal;

@Name("bond.BondIssued")
@Label("Bond Issued Event")
@Description("Tracks when a new bond is issued for any client")
@StackTrace(false)
public class BondIssuedJfrEvent extends Event {

    @Label("Client Identifier")
    private Long clientId;

    @Label("Bond Amount")
    private BigDecimal amount;

    @Label("Issued Timestamp in UTC")
    private long timestampInUtc;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public long getTimestampInUtc() {
        return timestampInUtc;
    }

    public void setTimestampInUtc(long timestampInUtc) {
        this.timestampInUtc = timestampInUtc;
    }
}
