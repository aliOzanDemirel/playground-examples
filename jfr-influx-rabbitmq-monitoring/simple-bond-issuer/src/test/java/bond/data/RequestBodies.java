package bond.data;

import bond.dto.request.BondRequest;

import java.math.BigDecimal;

public class RequestBodies {

    private RequestBodies() {
    }

    public static BondRequest getBondRequest(long clientId, int term, BigDecimal amount) {

        var body = new BondRequest();
        body.setClientId(clientId);
        body.setTerm(term);
        body.setAmount(amount);
        return body;
    }

}
