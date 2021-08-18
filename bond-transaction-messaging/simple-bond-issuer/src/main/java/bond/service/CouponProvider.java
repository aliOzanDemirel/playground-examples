package bond.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CouponProvider {

    // this can be some kind of coupon management as a proper service with persistent configuration for interest rate
    // or minimum term or some other metadata. for the sake of simplicity there is only one default coupon in the application.
    private CouponProvider() {
    }

    public static final double DEFAULT_INTEREST_RATE = 5D;
    public static final int DEFAULT_MINIMUM_TERM = 5;

    static Double interestRateAfterTermExtension(Double interestRate) {

        return interestRate - interestRate * 10 / 100;
    }

    public static BigDecimal calculateReturnAmount(BigDecimal baseAmount, int term, double interestRate) {

        // (base amount * length of term (in years) * interest rate (in percentage)) / 100 (because rate is percentage)
        return baseAmount
                .multiply(BigDecimal.valueOf(interestRate))
                .multiply(BigDecimal.valueOf(term))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

}
