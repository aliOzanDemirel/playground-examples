package bond.validator;

import bond.repository.BondRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.math.BigDecimal;
import java.time.LocalTime;

@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class BondSaleValidator implements ConstraintValidator<BondSaleValidation, Object[]> {

    // can be configurable
    private static final int BOND_AMOUNT_LIMIT_AT_NIGHT = 1000;
    private static final int DAILY_MAX_LIMIT_FOR_IP = 5;

    private BondRepository bondRepository;

    @Autowired
    public BondSaleValidator(BondRepository bondRepository) {
        this.bondRepository = bondRepository;
    }

    @Override
    public boolean isValid(Object[] parameters, ConstraintValidatorContext context) {

        Long clientId = (Long) parameters[0];
        String sourceIp = (String) parameters[3];
        BigDecimal amount = (BigDecimal) parameters[2];

        if (!soldBondLimitIsNotExceeded(clientId, sourceIp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bond limit is exceeded for " + sourceIp);

        } else if (!isValidAmountForNightHours(amount)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Bond amount cannot be higher than " + BOND_AMOUNT_LIMIT_AT_NIGHT + " between 22:00 and 06:00");
        }
        return true;
    }

    private boolean soldBondLimitIsNotExceeded(Long clientId, String sourceIp) {

        return bondRepository.countSoldBondsForClientAndSourceIp(clientId, sourceIp) < DAILY_MAX_LIMIT_FOR_IP;
    }

    private boolean isValidAmountForNightHours(BigDecimal amount) {

        if (amount.compareTo(BigDecimal.valueOf(BOND_AMOUNT_LIMIT_AT_NIGHT)) >= 0) {

            return !isTimeInNightHours(LocalTime.now());
        }
        return true;
    }

    public boolean isTimeInNightHours(LocalTime time) {

        return (time.isAfter(LocalTime.of(0, 0, 0, 0)) && time.isBefore(LocalTime.of(6, 0))) ||
                (time.isAfter(LocalTime.of(22, 0)) && time.isBefore(LocalTime.of(23, 59, 59, 999999)));
    }

}
