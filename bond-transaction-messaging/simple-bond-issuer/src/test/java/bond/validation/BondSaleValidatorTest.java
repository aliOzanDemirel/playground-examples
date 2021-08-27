package bond.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import bond.repository.BondRepository;
import bond.validator.BondSaleValidator;

import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class BondSaleValidatorTest {

    @Mock
    private BondRepository bondRepository;
    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    private BondSaleValidator validator;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        validator = spy(new BondSaleValidator(bondRepository));
    }

    @Test
    public void testFailValidationBecauseOfIpLimit() {

        var clientId = 5L;
        var sourceIp = "9.8.7.6";
        var amount = BigDecimal.valueOf(500);

        given(bondRepository.countSoldBondsForClientAndSourceIp(clientId, sourceIp)).willReturn(6);

        Object[] methodParams = {clientId, 10, amount, sourceIp};
        try {
            validator.isValid(methodParams, constraintValidatorContext);
            Assert.fail();

        } catch (ResponseStatusException ex) {

            verify(bondRepository, times(1)).countSoldBondsForClientAndSourceIp(clientId, sourceIp);

            Assert.assertNotNull(ex.getStatus());
            Assert.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            Assert.assertNotNull(ex.getReason());
            Assert.assertTrue(ex.getReason().startsWith("Bond limit is exceeded for"));
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
            Assert.fail();

        } catch (ResponseStatusException ex) {

            verify(bondRepository, times(1)).countSoldBondsForClientAndSourceIp(clientId, sourceIp);

            Assert.assertNotNull(ex.getStatus());
            Assert.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            Assert.assertNotNull(ex.getReason());
            Assert.assertTrue(ex.getReason().startsWith("Bond amount cannot be higher than"));
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
            Assert.assertTrue(isValid);

        } catch (ResponseStatusException ex) {

            Assert.fail();
        }
    }

}