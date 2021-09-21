package transaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import transaction.controller.response.StatisticsResponse;
import transaction.service.TransactionCache;
import transaction.service.TransactionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TransactionServiceTest {

    private TransactionCache cache;
    private TransactionService transactionService;

    private static final long TEST_AMOUNT_COUNT = 4;
    private BigDecimal testMaxAmount;
    private BigDecimal testMinAmount;
    private BigDecimal testAmount_1;
    private BigDecimal testAmount_2;

    @BeforeEach
    public void before() {

        int roundingScale = 2;
        long validityTimeout = 60_000;
        cache = Mockito.mock(TransactionCache.class);
        transactionService = new TransactionService(cache, validityTimeout, roundingScale);

        testMaxAmount = BigDecimal.valueOf(20).setScale(roundingScale, RoundingMode.HALF_UP);
        testMinAmount = BigDecimal.valueOf(-8).setScale(roundingScale, RoundingMode.HALF_UP);
        testAmount_1 = BigDecimal.valueOf(0.8).setScale(roundingScale, RoundingMode.HALF_UP);
        testAmount_2 = BigDecimal.valueOf(-4).setScale(roundingScale, RoundingMode.HALF_UP);

        BigDecimal[] amounts = {testAmount_1, testAmount_2, testMaxAmount, testMinAmount};
        given(cache.getTransactionsByTimeout(validityTimeout)).willReturn(Arrays.asList(amounts));
    }

    @Test
    public void testGetTransactionStatistics() {

        StatisticsResponse statistics = transactionService.getTransactionStatistics();

        Assertions.assertEquals(TEST_AMOUNT_COUNT, statistics.getCount());
        Assertions.assertEquals(testMaxAmount, statistics.getMaxD());
        Assertions.assertEquals(testMinAmount, statistics.getMinD());

        BigDecimal expectedSum = testMaxAmount.add(testMinAmount).add(testAmount_1).add(testAmount_2);
        Assertions.assertEquals(expectedSum, statistics.getSumD());

        BigDecimal expectedAvg = expectedSum.divide(BigDecimal.valueOf(TEST_AMOUNT_COUNT), RoundingMode.HALF_UP);
        Assertions.assertEquals(expectedAvg, statistics.getAvgD());

        verify(cache, times(1)).getTransactionsByTimeout(anyLong());
    }
}
