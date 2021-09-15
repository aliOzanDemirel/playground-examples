package transaction;

import org.junit.Assert;
import org.junit.Test;
import transaction.controller.mapper.TransactionRequestMapper;
import transaction.controller.request.TransactionRequest;
import transaction.domain.Transaction;
import transaction.exception.InvalidInputException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static transaction.controller.mapper.TransactionRequestMapper.TRANSACTION_TIMESTAMP_INPUT_FORMATS;


public class TransactionRequestMapperTest {

    @Test
    public void testBuildTransactionSuccess() {

        LocalDateTime someValidDate = LocalDateTime.of(2018, Month.OCTOBER, 6, 11, 27, 33, 478000000);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(TRANSACTION_TIMESTAMP_INPUT_FORMATS.get(0));

        String[] amounts = {"11", "27.0", "333.000000000000003", "1965.947813", "874515.9478139945"};
        Arrays.asList(amounts).forEach(amount -> {

            String formattedValidDate = someValidDate.format(dtf);
            Transaction transaction = null;
            try {
                transaction = TransactionRequestMapper.convertTransactionRequestBody(new TransactionRequest(amount, formattedValidDate));
            } catch (Exception e) {
                Assert.fail();
            }

            Assert.assertNotNull(transaction);
            Assert.assertEquals(BigDecimal.valueOf(Double.parseDouble(amount)), transaction.getAmount());

            long expected = LocalDateTime.parse(someValidDate.format(dtf), DateTimeFormatter.ofPattern(TRANSACTION_TIMESTAMP_INPUT_FORMATS.get(0))).toInstant(ZoneOffset.UTC).toEpochMilli();
            Assert.assertEquals(expected, transaction.getTimestamp());
        });
    }

    @Test
    public void testValid() throws Exception {
        TransactionRequestMapper.convertTransactionRequestBody(RequestBodies.REQ_VALID);
    }

    @Test(expected = InvalidInputException.class)
    public void testBuildTransactionAmountConversionFails() throws Exception {
        TransactionRequestMapper.convertTransactionRequestBody(RequestBodies.REQ_AMOUNT_INVALID);
    }

    @Test(expected = InvalidInputException.class)
    public void testBuildTransactionTimestampInvalidDateAndHour() throws Exception {
        TransactionRequestMapper.convertTransactionRequestBody(RequestBodies.REQ_TIMESTAMP_INVALID_DATE);
    }

    @Test(expected = InvalidInputException.class)
    public void testBuildTransactionTimestampInvalidFormat() throws Exception {
        TransactionRequestMapper.convertTransactionRequestBody(RequestBodies.REQ_TIMESTAMP_INVALID_FORMAT);
    }

    @Test
    public void testGetTimestampInMilli() {

        String[] testTimestamps = {"2018-10-06T10:25:40.178Z", "2018-10-06T18:20:30Z"};
        Arrays.asList(testTimestamps).forEach(it -> {
            long actual = TransactionRequestMapper.timestampInputToMillis(it);
            long expected = Instant.parse(it).toEpochMilli();
            Assert.assertEquals(expected, actual);
        });
    }

}
