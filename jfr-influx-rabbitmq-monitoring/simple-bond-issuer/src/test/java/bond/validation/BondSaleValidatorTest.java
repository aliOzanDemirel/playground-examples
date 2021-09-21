package bond.validation;

import bond.repository.BondRepository;
import bond.validator.BondSaleValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BondSaleValidatorTest {

    @Mock
    private BondRepository bondRepository;
    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Spy
    @InjectMocks
    private BondSaleValidator validator;

    @Test
    public void testFailValidationBecauseOfIpLimit() {

        var clientId = 5L;
        var sourceIp = "9.8.7.6";
        var amount = BigDecimal.valueOf(500);

        given(bondRepository.countSoldBondsForClientAndSourceIp(clientId, sourceIp)).willReturn(6);

        Object[] methodParams = {clientId, 10, amount, sourceIp};
        try {
            validator.isValid(methodParams, constraintValidatorContext);
            Assertions.fail();

        } catch (ResponseStatusException ex) {

            verify(bondRepository, times(1)).countSoldBondsForClientAndSourceIp(clientId, sourceIp);

            Assertions.assertNotNull(ex.getStatus());
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            Assertions.assertNotNull(ex.getReason());
            Assertions.assertTrue(ex.getReason().startsWith("Bond limit is exceeded for"));
        }
    }

    @Test
    public void testFailValidationBecauseOfNightHours() {

        var clientId = 5L;
        var sourceIp = "9.8.7.6";
        var amount = BigDecimal.valueOf(1001);

        given(bondRepository.countSoldBondsForClientAndSourceIp(clientId, sourceIp)).willReturn(3);
        doReturn(true).when(validator).isTimeInNightHours(any());

        Object[] methodParams = {clientId, 10, amount, sourceIp};
        try {
            validator.isValid(methodParams, constraintValidatorContext);
            Assertions.fail();

        } catch (ResponseStatusException ex) {

            verify(bondRepository, times(1)).countSoldBondsForClientAndSourceIp(clientId, sourceIp);

            Assertions.assertNotNull(ex.getStatus());
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            Assertions.assertNotNull(ex.getReason());
            Assertions.assertTrue(ex.getReason().startsWith("Bond amount cannot be higher than"));
        }
    }

    @Test
    public void testSuccessfulValidation() {

        var clientId = 5L;
        var sourceIp = "9.8.7.6";
        var amount = BigDecimal.valueOf(1040);

        given(bondRepository.countSoldBondsForClientAndSourceIp(clientId, sourceIp)).willReturn(3);
        doReturn(false).when(validator).isTimeInNightHours(any());

        Object[] methodParams = {clientId, 10, amount, sourceIp};
        try {
            var isValid = validator.isValid(methodParams, constraintValidatorContext);
            verify(bondRepository, times(1)).countSoldBondsForClientAndSourceIp(clientId, sourceIp);
            Assertions.assertTrue(isValid);

        } catch (ResponseStatusException ex) {

            Assertions.fail();
        }
    }
}
