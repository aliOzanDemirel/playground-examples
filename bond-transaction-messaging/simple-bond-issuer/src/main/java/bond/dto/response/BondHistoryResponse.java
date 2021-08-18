package bond.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import bond.domain.BondHistory;

import java.time.Instant;

@Data
@NoArgsConstructor
public class BondHistoryResponse {

    public BondHistoryResponse(BondHistory history) {

        action = history.getAction();
        interestRate = history.getInterestRate();
        term = history.getTerm();
        created = history.getCreatedDate();
    }

    private BondHistory.Action action;
    private Double interestRate;
    private Integer term;
    private Instant created;

}
