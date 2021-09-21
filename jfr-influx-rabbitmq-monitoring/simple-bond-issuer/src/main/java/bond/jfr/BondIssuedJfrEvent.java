package bond.jfr;

import jdk.jfr.*;

@Name("bond.BondIssued")
@Label("Bond Issued Event")
@Description("Tracks when a new bond is issued for any client")
@StackTrace(false)
public class BondIssuedJfrEvent extends Event {

    @Label("Issued Timestamp in UTC")
    private long timestampInUtc = -1;

    @Label("Client Identifier")
    private long clientId = -1;

    @Label("Bond Amount")
    private String amount = null;

    public long getTimestampInUtc() {
        return timestampInUtc;
    }

    public void setTimestampInUtc(long timestampInUtc) {
        this.timestampInUtc = timestampInUtc;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
