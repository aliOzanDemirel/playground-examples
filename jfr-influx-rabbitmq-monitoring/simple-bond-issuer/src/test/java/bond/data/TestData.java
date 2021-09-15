package bond.data;

import bond.domain.Bond;
import bond.domain.BondHistory;

import java.math.BigDecimal;
import java.time.Instant;

public class TestData {

    public static final Instant DUMMY_CREATED_DATE = Instant.now();
    public static final String DUMMY_SOURCE_IP = "1.2.3.4";

    private TestData() {
    }

    public static BondHistory createBondHistory(long id, long bondId) {

        var entity = new BondHistory();
        entity.setId(id);
        entity.setAction(BondHistory.Action.UPDATED);
        entity.setTerm((int) id * 2);
        entity.setInterestRate((double) (id * 3));
        entity.setBond(createBond(bondId));
        entity.setCreatedDate(DUMMY_CREATED_DATE);
        return entity;
    }

    public static Bond createBond(long bondId) {

        return createBond(bondId, bondId, (int) bondId * 2, (double) bondId * 3, BigDecimal.valueOf(bondId * 10));
    }

    public static Bond createBond(long bondId, long clientId, int term, double interestRate, BigDecimal amount) {

        var entity = new Bond();
        entity.setId(bondId);
        entity.setClientId(clientId);
        entity.setTerm(term);
        entity.setInterestRate(interestRate);
        entity.setAmount(amount);
        entity.setSourceIp(DUMMY_SOURCE_IP);
        entity.setCreatedDate(DUMMY_CREATED_DATE);
        return entity;
    }

}
