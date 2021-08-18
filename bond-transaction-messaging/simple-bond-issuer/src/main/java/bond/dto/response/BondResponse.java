package bond.dto.response;

import bond.domain.Bond;
import bond.service.CouponProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
public class BondResponse {

    public BondResponse(Bond bond) {

        bondId = bond.getId();
        clientId = bond.getClientId();
        issuedDate = bond.getCreatedDate();
        term = bond.getTerm();
        interestRate = bond.getInterestRate();
        amount = bond.getAmount();
        returnAmount = CouponProvider.calculateReturnAmount(amount, term, interestRate);
    }

    private Long bondId;
    private Long clientId;

    /**
     * format: {@link java.time.format.DateTimeFormatter#ISO_INSTANT}
     */
    private Instant issuedDate;
    private Integer term;

    private Double interestRate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal returnAmount;

}
